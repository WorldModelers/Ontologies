# Ontologies

[![Build Status](https://travis-ci.com/WorldModelers/Ontologies.svg?branch=master)](https://travis-ci.com/WorldModelers/Ontologies)<sup>1</sup>
[![](https://jitpack.io/v/WorldModelers/Ontologies.svg)](https://jitpack.io/#WorldModelers/Ontologies)<sup>2</sup>

<sup>1</sup>This project is integrated with Travis so that each commit provokes unit tests to ensure that the ontology file(s) are in relatively good shape.

<sup>2</sup>It is furthermore connected to jitPack so that build tools like gradle, maven, sbt, or leiningen can retrieve their dependencies directly from GitHub.  Click on the jitPack icon for configuration instructions.

## Note

* The current flat ontology is contained in wm_flat.yml and its companion wm_flat_metadata.yml.
* The current compositional ontology is contained in CompositionalOntology.yaml and its companion CompositionalOntology_metadata.yml.

All other ontologies have been moved to the deprecated directory. 

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

Described here is an updated format, `fmt2`.  See [lastFmt1](https://github.com/WorldModelers/Ontologies/blob/lastFmt1/README.md) for the last use of `fmt1`.

Nodes in the ontology are formatted as such:
```yml
node:
    name: [name_of_node]
    descriptions:
        - [description1]
        - ...
        - [descriptionL]
    patterns:
        - [pattern1]
        - ...
        - [patternM]
    examples:
        - [example1]
        - ...
        - [exampleN]
    opposite: [path/to/opposite]
    polarity: [1 | -1]
    semantic type: [entity | event | property]
    children:
        - node:
            name: ...
        - node:
            name: ...  
```
where all the keys (`node`, `name`, `descriptions`, `patterns`, `examples`, `opposite`, `polarity`, `semantic type`, and `children`)
should be reproduced verbatim when they are used.  If they are not used, e.g., there is no `example1`, then the key, `example` here, should not be listed.  The parser doesn't like superfluous keys.  `node` and `name` are the only keys required.  Not all nodes need to have the same set of keys.

Values are described briefly in the table below.

|Key|Description of Value|
|---|---|
|name|The name of the node, used for identification purposes.  Spaces should be replace by underscores.  Slashes should be avoided.|
|descriptions|Longer texts that define the node or exemplify the context in which it might be found|
|patterns|Regular expressions that might be used to identify text that should match the node|
|examples|Short phrases, possibly synonyms, that should match the node|
|opposite|A /-separated path to an ontology node with the same meaning but of opposite polarity|
|polarity|Either `1` or `-1`.  Opposites should have opposite polarity.|
|semantic type|Either `entity`, `event`, or `property`|
|children|A list is child nodes, each explicitely denoted with key `node`|

Here are some examples based on real entries:
```yml
- node:
    name: irrigation
    descriptions:
        - The process of applying controlled amounts of water to plants.
    examples:
        - irrigate
        - irrigating
        - irrigation
    polarity: 1
    semantic type: event
- node:
    name: hopper_band
    patterns:
        - (hopper\s+band)|(bands?\s+of\s+hoppers)
    examples:
        - bands of hoppers
        - locust hopper groups
        - hopper outbreak upsurge
        - immature
    polarity: -1
- node:
    name: crop
    children:
        - node:
            name: cereals
            descriptions:
                - A grass cultivated for the edible parts of its grain.
            examples:
                - barley
                - cereals
                - maize
                - sorghum
                - tef
                - wheat
            polarity: 1
            semantic type: entity
        - node:
            name: crop_land
```

Branch nodes, i.e., nodes with children, can in `fmt2` have all the data associated with leaf nodes, those without children.  Branches with no leaves and no data should be avoided.

The root branch, usually named `wm`, is presently an object/dictionary but may soon be turned into a list.

```yml
node:
    name: wm
    children:
        - node:
            ...
        - node:
            ...    
```

## Version

If you use the jar file that is produced by [jitpack](https://jitpack.io/#WorldModelers/Ontologies) to encorporate the ontologies into your project, you  should find two properties files next to the ontology files which record their versions according to git.  `CompositionalOntology_metadata.properties`, for example, looks something like this:

```
hash = bbafee6c4eeb5f94cd746aceed21b48e5b5eae1a
date = 2021-10-05T17:48:19Z
```

## Tests

The unit tests presently check for
* yml syntax errors
* special characters that might have been copied in unnecessarily
* duplicate paths
* duplicate leaf nodes
* unlabeled nodes
* spaces in path
* mismatched opposites
* missing examples
* valid polarity
* valid semantic types
* unknown keys

If a test fails, there may be a hint to the reason why in the output which shows nodes as they
are processed.  The last node printed, or one after, is the likely problem case.
