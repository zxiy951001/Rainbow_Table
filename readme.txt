Read me *Important
Executing System Security Rainbow Program
Assignment 1 Part 2
=================================
Compile: javac Rainbow.java
Execution: java Rainbow Passwords.txt

*Passwords.txt in same directory as Rainbow.class file

Reduction function
------------------

(1) Given MD5 hash H,
(2) Convert H from hexadecimal into a (big) integer, B
(3) Take index i to be the B modulo size of dictionary, i.e. i = B % 1000
(4) Find the word w at index i in our list of words
(5) Return w



*Passwords.txt is Wordlist.txt provided
*To reduce a different txt file, during compilation run ---> java Rainbow [textfilename].txt
