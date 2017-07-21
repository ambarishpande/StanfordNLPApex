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


