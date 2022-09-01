def func2(dict_val):
    retval = []
    for key, val in dict_val.items():
        retval.append(key + str(val))

    retval.sort()
    return retval

import sys

if __name__ == "__main__":
    args = sys.argv[1:]
    new_args = [eval(arg) for arg in args]
    print (func2(*new_args))