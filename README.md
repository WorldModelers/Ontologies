# Ontologies

[![Build Status](https://travis-ci.org/WorldModelers/Ontologies.svg?branch=master)](https://travis-ci.org/WorldModelers/Ontologies)<sup>1</sup>
[![](https://jitpack.io/v/WorldModelers/Ontologies.svg)](https://jitpack.io/#WorldModelers/Ontologies)<sup>2</sup>

<sup>1</sup>This project is integrated with Travis so that each commit provokes unit tests to ensure that the ontology file(s) are in relatively good shape.

<sup>2</sup>It is furthermore connected to jitPack so that build tools like gradle, maven, sbt, or leiningen can retrieve their dependencies directly from GitHub.  Click on the jitPack icon for configuration instructions.

## Procedure

When working on these files with other programs, please follow these steps:

1. Clone the repo
1. Create a separate branch for your work
1. Make your changes
1. Push the branch back here
1. Check that tests have been run
1. Account for any failing tests
1. Create a pull request
1. Check that tests will also pass for the merged version
1. Accept the pull request and merge
1. Update any other projects that depend on the changes

If you want to make edits directly through the GitHub user interface, please be sure to select the option "Create a new branch for this commit and start a pull request." when submitting the changes.  This allows the unit tests to be run at the right time.

## Format

Leaf nodes in the ontology with metadata are presently formatted as such:
```yml
- OntologyNode:
  pattern:
  - pattern1
  - ...
  - patternN
  examples:
  - example1
  - ...
  - exampleN
  descriptions:
  - description1
  - ...
  - descriptionN
  name: name
  polarity: 1.0

```
where all the keys (`OntologyNode`, `pattern`, `examples`, `descriptions`, `name`, `opposite`, and `polarity`)
should be reproduced verbatim when they are used.  `name` is the only one required.  Not all nodes
need to have the same set of keys.

Values are described briefly in the table below.

|Key|Description of Value|
|---|---|
|pattern|A regular expression that might be used to identify text that should match the node|
|examples|Short phrases, possibly synonyms, that should match the node|
|descriptions|Longer texts that define the node or exemplify the context in which it might be found|
|name|The name of the node, used for identification purposes|
|opposite|A /-separated path to an ontology node with the same meaning but of opposite polarity|
|polarity|Presently always 1, but potentially -1 to use for opposites|

Here are two examples based loosely on real entries:
````yml
- OntologyNode:
  pattern:
  - (intervention)
  - (humanitarian)(\s|\w)+(aid|assistance)
  examples:
  - access
  - humanitarian response
  - humanitarian aid
  - poverty alleviation
  name: humanitarian assistance
  opposite: wm/concept/.../water_security
  polarity: 1
````
```yml
- OntologyNode:
  descriptions:
  - Upper secondary school pupil-teacher ratio is the average number of pupils per
    teacher in upper secondary school.
  name: Pupil-teacher_ratio,_upper_secondary
  opposite: wm/concept/.../water_insecurity
  polarity: -1
```

Branch nodes have only a branch name and have no data associated with them other than
their position in the hierarchical list and their potential leaves:

````yml
- wm
  - branch 1
    - branch 1.1
      - OntologyNode:
      ...
      - OntologyNode:
      ...
    - branch 1.2
      - branch 1.2.1
        - OntologyNode:
      ...
  - branch 2
    - OntologyNode:
    ...
  - branch 3
````

The root branch, `wm` here, usually identifies the ontology itself.
Branches with no leaves should be avoided.

Here's a small, but concrete example:

```yml
- wm:
  - concept:
    - causal_factor:
      - intervention:
        - OntologyNode:
          name: humanitarian assistance
        - provision of goods and services:
          - education:
            - OntologyNode:
              name: child friendly learning spaces
          - health:
            - OntologyNode:
              name: anti-retroviral treatment
            - OntologyNode:
              name: sexual violence management
```

## Tests

The unit tests presently check for
* yml syntax errors
* special characters that might have been copied in unnecessarily
* duplicate paths
* duplicate leaf nodes
* spaces in path
* mismatched opposites
