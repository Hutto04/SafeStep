// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        maven { url = uri("https://chaquo.com/maven") }
    }
    dependencies {
        classpath ("com.chaquo.python:gradle:10.0.1")
    }
}


plugins {
    id("com.android.application") version "8.3.1" apply false
    id("com.chaquo.python") version "15.0.1" apply false
}