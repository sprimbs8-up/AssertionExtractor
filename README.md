# Assertion Extractor

The task of this project is to extract assertions from a given test case. On a larger scale, it serves as a
pre-processing tool that translates the assertions from raw data containing a test method and a focal method into
several different formats.

## Requirements:

- Java 17

## Usage:

The Tool supports two different options:
Firstly, preprocessing can be used for various assertion models, and secondly, given assertions can be checked for
validity.

### Build JAR file:

```bash
    bash gradlew runnableJar
```

### Preprocessing:
#### Args:
It supports the following commands:

- `-m`, `--max-assertions`: The number of maximal assertions per test case.
- `d`, `--data-file`: The directory of the data source in jsonl format.
- `s`, `--save-dir`: The directory to save the preprocessed data.
- `--model`: The model for that the data should be parsed. You can enter the following
  models: `atlas:toga:code2seq:asserT5:gpt`. More than one model is allowed when separating them with commas.
- `--seed`: The seed for the Randomness. By default it is set to 1. Recommendation: Do not change the seed when preprocessing the data.

#### Example Command:
```bash
    java -jar [path_to_java_jar] preprocess --data-file example-files/results-small.jsonl --save-dir preprocessed -m 5 --model toga:atlas:asserT5 
```
#### Script:
There is a script to automate this preprocessing. Enter:
```bash
    bash build-all.sh [path_to_results_jsonl_file] [save_dir] ([number_assertions_separated]) ([models_separated]) 
```
### Assertion Validation:
In the evaluation script the validity of the assertions must be proven. For that an extra subcommand solves that:
#### Args:
- `-c`, `--codes`: The assertion statements codes in json list format.

#### Example Command:
```bash
    java -jar [path_to_java_jar] check --codes "[\"assertEquals(res, 3)\"]"
```