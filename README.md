# About
pplex is an application that can interpret linear programs (LP) and show
how the LP dictionary evolves throughout the execution of the simplex method.

pplex is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

pplex uses Apache Commons Math library for handling matrices. It is released
under the Apache License, Version 2.0. See lib/LICENSE.TXT for more information.

![pplex supports visualization](https://raw.github.com/andern/pplex/master/doc/teaching_simplex/ex_gui3.png)

[Try the pplex applet!](http://pplex.ii.uib.no)

# Documentation
Documentation on how to use pplex can be found [here](https://raw.github.com/andern/pplex/master/doc/pplex_intro.pdf).

## Reporting Bugs
GitHub's issue tracker is used for tracking bugs of pplex. Before submitting a
bug you should make sure that you are running the latest version of pplex, and
that the bug has not already been submitted to the tracker on GitHub.

# Installing pplex
If you want to try pplex before installing it, you can try it as an applet by
visiting http://pplex.ii.uib.no. It has most of the functionality
of pplex (but you cannot read your own files.)

Running pplex requires Java Runtime Environment installed on your computer.

1. Download the newest version of pplex from one of the links above.
2. Extract the archive.
3. Double-click on pplex.jar.
4. (Getting started) Type read input/vdb2.8.lps in pplex' console.

## Running pplex
pplex starts in visual mode by default. This is only relevant for linear
programs with two variables. pplex can easily be run without a graphical
interface in a terminal window by passing the '-nogui' argument like this:
  `java -jar pplex.jar -nogui`

# Compiling pplex
Compiling pplex requires:
* Java Development Kit
* Apache Ant

To compile pplex you first need to download the source code of pplex. This
can be done from the website or by cloning the repository.

To clone the repository you need git installed on your system. Cloning
the repository is done by typing the following in a terminal:
  `git clone git@github.com:andern/pplex.git`

To only compile the sources of pplex, run
  `ant compile`
in the pplex directory.

To compile the sources of pplex and build a runnable jar, run
  `ant jar`
  or simply
  `ant`

## Applet
pplex can be compiled as an applet by running
  `ant applet`
in the pplex directory.

See applet.html in the pplex directory for an example
on how to deploy the applet on your own website.
