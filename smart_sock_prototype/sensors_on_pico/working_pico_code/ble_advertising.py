from micropython import const
import struct
import bluetooth

# Advertising payloads are repeated packets of the following form:
#   1 byte data length (N + 1)
#   1 byte type (see constants below)
#   N bytes type-specific data

_ADV_TYPE_FLAGS = const(0x01)
_ADV_TYPE_NAME = const(0x09)
_ADV_TYPE_UUID16_COMPLETE = const(0x3)
_ADV_TYPE_UUID32_COMPLETE = const(0x5)
_ADV_TYPE_UUID128_COMPLETE = const(0x7)
_ADV_TYPE_UUID16_MORE = const(0x2)
_ADV_TYPE_UUID32_MORE = const(0x4)
_ADV_TYPE_UUID128_MORE = const(0x6)
_ADV_TYPE_APPEARANCE = const(0x19)

# Generate a payload to be passed to gap_advertise(adv_data=...).
def advertising_payload(name=None, services=None, appearance=0, manufacturer_data=None):
    payload = bytearray()

    def _append(adv_type, value):
        nonlocal payload
        payload += struct.pack("BB", len(value) + 1, adv_type) + value

    if manufacturer_data:
        # Prepend the manufacturer data with its AD type, which is 0xFF for Manufacturer Specific Data
        _append(0xFF, manufacturer_data)

    if services:
        for uuid in services:
            b = bytes(uuid)
            if len(b) == 2:
                _append(0x03, b)  # AD type for 16-bit UUID
            elif len(b) == 4:
                _append(0x05, b)  # AD type for 32-bit UUID
            elif len(b) == 16:
                _append(0x07, b)  # AD type for 128-bit UUID

    if name:
        _append(0x09, name.encode())  # AD type for complete local name

    if appearance:
        _append(0x19, struct.pack("<h", appearance))  # AD type for appearance

    return payload



def decode_field(payload, adv_type):
    i = 0
    result = []
    while i + 1 < len(payload):
        if payload[i + 1] == adv_type:
            result.append(payload[i + 2 : i + payload[i] + 1])
        i += 1 + payload[i]
    return result


def decode_name(payload):
    n = decode_field(payload, _ADV_TYPE_NAME)
    return str(n[0], "utf-8") if n else ""


def decode_services(payload):
    services = []
    for u in decode_field(payload, _ADV_TYPE_UUID16_COMPLETE):
        services.append(bluetooth.UUID(struct.unpack("<h", u)[0]))
    for u in decode_field(payload, _ADV_TYPE_UUID32_COMPLETE):
        services.append(bluetooth.UUID(struct.unpack("<d", u)[0]))
    for u in decode_field(payload, _ADV_TYPE_UUID128_COMPLETE):
        services.append(bluetooth.UUID(u))
    return services


def demo():
    payload = advertising_payload(
        name="micropython",
        #services=[bluetooth.UUID(0x181A), bluetooth.UUID("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")],
    )
    print(payload)
    print(decode_name(payload))
    print(decode_services(payload))


if __name__ == "__main__":
    demo()