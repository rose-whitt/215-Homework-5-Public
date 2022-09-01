def func1(bool_val, int_val, float_val):
    if bool_val:
        return int_val * float_val
    else:
        return int_val + float_val

import sys

if __name__ == "__main__":
    args = sys.argv[1:]
    new_args = [eval(arg) for arg in args]
    print (func1(*new_args))