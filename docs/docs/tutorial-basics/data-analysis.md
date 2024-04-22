---
sidebar_position: 7
---

# Data Analysis

Here we cover our data analysis methodology so far as well as were we would like to go. 

## Data Pipeline Goals

**Our goals include the following:**

1. Develop methods that categorize data as either typical or atypical of diabetic / non-diabetic patients. Please note that a reading may not be typical of either case.

2. Create infrastructure for training an AI model using filtered data (from the methods above) to predict with high accuracy how likely a patient is to be diabetic. This model will mark return an abnormality score for data within a certain window.

3. Create a pipeline that can ingest live data and classify using a deployed version of the above AI model to classify user data.

The goals are flexible and may change as our expectations of the system and the user interface evolve. Hence, learning the nature of the dataset is crucial; it will determine our expectations and frontend experience.

## Analysis of Initially Provided Data

At the start of the project, the team was given a handful of MS Excel files (.xlsx) to learn from. To sum up our findings, these files were found to be nearly useless.

The features in the files include: Time, Date, `MTK1.T`, `MTK2.T`, `MTK3.T`, `MTK4.T`, `MTK5.T`, `D1.T`, `L.T`, `C.T`, `Env.T`, `MTK1.P`, `MTK2.P`, `MTK3.P`, `MTK4.P`, `MTK5.P`, `D1.P`, `L.P`, and `C.P`.

All features that end in “`.T`” are temperature readings and all features that end in “`.P`” are pressure readings. `MTK` refers to metatarsals, `D1` to digit 1 (large toe), `L` to lateral, and `C` to calcaneus.

To gain insight into the characteristics of the data, we start by ingesting arbitrary data files and then plotting the data along certain features, as each file contains several dimensions.

### Notable issues

**The following are notable issues discovered while analyzing the initial data files:**

- There is no complete information on the conditions, equipment, and procedures used to gather the data

- Each file covers about 100 minutes of continuous use per patient, which is inadequate for model training

- Many features seem to be on completely different scales (both scaling factor and offset) depending on the source—for example, `MTK.P` ranges between 0 and 5000 temperature units in diabetic files and ranges between 0 and 30000 temperature units in non-diabetic files, and such a discrepancy cannot be caused by diabetes alone

- There is no clear complete trend captured for some dimensions—for example, `Env.T` for many files is nearly monotonically increasing.

- Many files consist mostly of outlier data points (compared to other files with similar cases)

- Some features, such as `C.T`, see vary wildly in shape and magnitude depending on the file

After this initial analysis, we realized the goal of acquiring high-quality data with the desired schema was going to be challenging, so we turned much of our attention to designing smart sock hardware capable of recording temperature and/or pressure.

## Tool Development

We have developed some code that achieve some useful functions. Whether those functions are useful in the scope of the project will be determined by future investigation. The following are summaries and rationales for the tools. All are at the projects disposal for creating an affective pipeline, whether used purely for training, for presenting to the user directly, or otherwise.

### Ingest:
    We have created a method for reading in all Excel files given the directly system seen in the GitHub repository.  This is likely temporary as it is meant for a testing notebook environment.

### Plotting:
    There are a handful of functions for plotting as well as examples, all of which use Matplotlib.

### Cleaning:
    Regardless of the irregularity of the data, our intuition was that we needed some method of removing outlier readings from the data. These include unexplainable spikes whose origin are likely measuring hardware errors. No matter the origin, our machine learning model will likely function best when working with only high-quality data
    
    One method of implemented method of outlier detection is IQR. The idea was to eliminate a lot of values, ensuring we discard the most irregular points.  This way, we know all data left is clean. However, the varying shapes have shown this is not viable for the initially provided data files.

### Global statistics:
    We have provided for getting basic global statistics given a data frame from a file.

### Smoothening:
    We have provided smoothening functions that apply a kernel (or some windowing technique) across a data frame to produce another data frame where, hopefully, many of the local outliers are removed. Our worry was outliers in “valleys” that are hard to detect using global outlier detection. This, unfortunately, also reduced sharp edges in the graph. Smoothening may use rolling mean, rolling median, or a gaussian kernel.

### Feature engineering:
    We wanted to engineer new features that we thought may increase the accuracy of primitive AI models. Hence, we created some functions for building new data frames with features extracted from the passed data frame. This includes near-instantaneous tangents (`a[i, j] = a[i, j] - a[i+1, j]`) and rolling standard deviation.

