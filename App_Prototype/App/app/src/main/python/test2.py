import matplotlib.pyplot as plt
import io

def generate_graph(x, y):
    # Create a figure and axis
    fig, ax = plt.subplots()
    
    # Define a list of colors for the bars
    colors = ['#8c5109', '#bf812d', '#35978f', '#7fcdc1', '#c7eae5', '#c7eae5', '#dfc27d', '#00665d']

    # Plot the data as a bar graph
    ax.bar(x, y, color=colors)
    
    # Set labels and title
    ax.set_xlabel('Sensor Name')
    ax.set_ylabel('Pressure')
    ax.set_title('Bar Graph')
    
    # Save the plot to a BytesIO object
    buf = io.BytesIO()
    fig.savefig(buf, format='png')
    buf.seek(0)
    
    # Return the byte array
    return buf.getvalue()