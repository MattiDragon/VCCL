# VCCL
Variable Controlled Commandline Language is an esoteric programming language that relies on 
variables for I/O and control flow. This repo contains an interpreter programmed in java. You 
can also find some example code from the "examples" directory.

# Example Code
Truth Machine:
```
in: string_in
is0: in = #"0"
is1: in = #"1"
isValid: is0 | is1
isInvalid: ! isValid
pointer: isInvalid ? #-1 : #7
bool: is1
pointer: bool ? #9 : #11
string_out: #"1"
pointer: #9
string_out: #"0"
```

# Links