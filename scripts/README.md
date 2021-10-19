Most of the Python scripts in this directory were originally written with ontology format `fmt1` in mind.  Here are some notes on how to deal with `fmt2`:

* convert_csv_to_yaml.py - Convert the csv file first to `fmt1` and then use the Scala App Convert_1_2_App to complete conversion to `fmt2`.
* convert_owl_to_yaml.py - Similarly convert the owl file first to `fmt1` and then use the Scala App `Convert_1_2_App` to complete conversion to `fmt2`.
* factorial.py - This is only here to double check the Python testing framework.  No change is necessary.
* flatten_compositional.py - This hasn't been updated.
* get_leaf_node_examples.py - This hasn't been updated.
* merge_yamls.py - This hasn't been updated.
* strip_metadata.py - This has been converted to the `StripMetadataApp` in the extras subproject.
