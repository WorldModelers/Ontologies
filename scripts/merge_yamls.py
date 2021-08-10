import io
import sys
import hiyapyco


def main():
    if len(sys.argv) != 4:
        print("usage: python merge_yamls.py [path_to_input_file_1] [path_to_input_file_2] [output_file]")
    else:
        merged_yaml = hiyapyco.load(sys.argv[1], sys.argv[2], method=hiyapyco.METHOD_MERGE)
        merged = hiyapyco.dump(merged_yaml)
        with io.open(sys.argv[3], "w") as wm:
            wm.write(merged)
        print("Successfully merged yaml files!")


if __name__ == '__main__':
    main()
