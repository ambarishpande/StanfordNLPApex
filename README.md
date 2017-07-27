# Natural Language Processing with Stanford CoreNLP with Apache Apex.
This repository contains Apache Apex operator to perform NLP tasks along with some Demo Applications.

## Build Intructions

```
mvn clean install -DskipTests
```

## Annotator Operator
A Generic Annotator Operator that uses Stanford NLP.

### Operator Capabilities
|No. |    Task                   |  Annotator Name |
|----|---------------------------|-----------------|
| 1) | Tokenization              |     tokenize    |
| 2) | Sentence Spliting         | 	 ssplit        |
| 3) | Parts of Speech Tagging   |   pos           |
| 4) | Lemmatization             |  	 lemma       |
| 5) | Named Entity Recognition  | ner             |
| 6) | Parsing                   | 	 parse         |
| 7) | Sentiment Analysis        |  sentiment      |

### Dependencies
1) Apache Apex 3.6.0+ 
2) Stanford Core Nlp Library v3.4.1
3) Stanford Core Nlp Models v3.4.1

### Usage

1) Set the prop.annotator through properties.xml as a comma separated list of required annotators.

```
<property>
  <name>apex.application.StanfordNLPApex.operator.Annotator.prop.annotator</name>
  <value>pos,parse</value>
</property>
```
2) Set prop.pipelineProperties through properties.xml for providing additional properties for annotators.

```
<property>
    <name>apex.application.StanfordNLPApex.operator.Annotator.prop.pipelineProperties</name>
    <value>{"ner.model" : "model.from.stanford-models.ser.gz"}</value>
</property>
```

To provide custom models to annotators launch the app with --libjars option in apex and give the paths to model files.

```
apex> launch StanfordNLPApex-1.0-SNAPSHOT.apa --libjars ~/path/to/model.ser.gz
```

## Twitter Sentiment Analysis Application

An Apex application to perform sentiment analysis of tweets.

### Steps to Run Application

1) Add your Twitter Api Credentials in the properties-TwitterSentimentAnalysis.xml file
2) Build application package using 
  ```
  mvn clean package -DskipTests
  ```
3) Download Twitter Pos Tagger Model from http://downloads.gate.ac.uk/twitie/gate-EN-twitter.model
4) launch the application via Apex CLI

```
apex> launch StanfordNLPApex-1.0-SNAPSHOT.apa --libjars ~/Downloads/gate-EN-twitter.model TwitterSentimentAnalysis
```
## ApexNLPClassifier Operator

A Stanford NLP classifier operator to classify text documents based on the provided pre trained model.

### Dependencies
Apart from dependencies of Annotator Operator.
1) Stanford Classifier

### Usage
1) Upload pretrained Stanford NLP model on hdfs.
2) Set modelFilePath in properties.xml to the path where the model is saved on HDFS.
```
<property>
    <name>apex.application.EmailSpam.operator.Classifier.prop.modelFilePath</name>
    <value>/path/to/email.model</value>
  </property>

```
3) Set classifierProperties in properties.xml to properties of classifier. Each property seperated by new line.
e.g.
```
   <property>
    <name>apex.application.EmailSpam.operator.Classifier.prop.classifierProperties</name>
    <value>
      useClassFeature = true
      1.splitWordsRegexp = \\s
      1.useSplitWords = true
      useNB = true
      goldAnswerColumn = 0
    </value>
  </property>
```

#### For Testing Performance of Trained model.
  1) Set testing property to true in properties.xml ( Default testing = false )
  ```
  <property>
    <name>apex.application.EmailSpam.operator.Classifier.prop.testing</name>
    <value>true</value>
  </property>
  ```
  2) Import the provided dashboard in Datatorrent Console to view Model performance Parameters.
 
 
  
##  Email Spam Example

An Apex application to demo the usage of ApexNLPClassifier Operator.

### Steps to Run the Application

1) Set the Emails.prop.directory property to the location of the test data.
```
<property>
    <name>apex.application.EmailSpam.operator.Emails.prop.directory</name>
    <value>path/to/data/</value>
</property>
 ```
2) Set ApexNLP operator properies as mentioned above.
2) Build application package using 
  ```
  mvn clean package -DskipTests
  ```
4) launch the application via Apex CLI

```
apex> launch StanfordNLPApex-1.0-SNAPSHOT.apa EmailSpam
```
