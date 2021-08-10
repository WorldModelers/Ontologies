import io
import sys
import hiyapyco


def main():
    if len(sys.argv) != 4:
        print("usage: python strip_metadata.py [yaml1] [yaml1] [output_file]")
    else:
        merged_yaml = hiyapyco.load(sys.argv[1], sys.argv[2], method=hiyapyco.METHOD_MERGE)
        error = False
        mergedLines = hiyapyco.dump(merged_yaml)
        if not error:
            with io.open(sys.argv[3], "w") as wm:
                wm.write(mergedLines)
            print("Successfully merged yaml files!")


if __name__ == '__main__':
    main()
