# Filesystem Optimizer

This program recognizes entries in the directory tree that are redundant or empty and outputs their paths (for further use).

## Features

### Redundancy

- Detect duplicate files

### Cleaning

- Detect empty files
- Detect empty folders

## How does it work?

The main purpose of this program is to detect duplicate files in specific file system paths. The program will traverse one directory at a time recursively (see: *crawl* below) and hash every file in every subdirectory and store it in a wrapper along with the filename extension and size (just paranoid about matching checksums, which is unlikely but still possible). If a file is found again, the program will notice this and print its (canonical) path to standard output. The first occurrence is never reported, so don't worry about data loss.

**Please note that some programs or the operating system may require a redundant file!**

It is recommended that you first direct the standard output to a file for inspection.

A pipe to *rm* without manually checking the file paths (which are output) is possible, but **NOT** recommended because - as already mentioned - **the output could contain critical system files that should not be deleted.**

**I am *not* responsible if you delete important files because the program has output their paths.**
***Always*** check the file paths **before** deleting to be sure everything is correct!

## Build

- Import in a Java capable IDE of your choice (IntelliJ is recommended)
- Compile the code

**Recommended if you want to use this program and are not interested in modifying its source code:**
- Use the prebuilt Java archive (=> Releases)

## Usage

This software requires the Java Runtime Environment (at least Version 8)

```
java -jar filesystem-optimizer.jar [option...]
```

The program uses **stderr** (System.err) for screen output and the interactive console.
File paths are output on **stdout** (System.out).

### Command Line Options / Arguments:

Option             | Short    | Arguments         | Default             | Description
:----------------: | :------: | :---------------: | :-----------------: | :------------------------------------------------------:
--directory-file   | -f       | \<file name\>     | none                | The program will crawl all paths in the specified file considering the other options (see *crawl* below). There must be exactly one folder path in each line of the given file.
--hash-algorithm   | -al      | \<algorithm\>     | SHA-256             | See: https://docs.oracle.com/javase/8/docs/api/java/security/MessageDigest.html
--list-duplicates  | -dup     | -                 | false               | Output of the file path if the file is already known, i.e. has already been hashed
--list-empty-dirs  | -ed      | -                 | false               | Output of empty directories
--list-empty-files | -ef      | -                 | false               | Output of empty files, i.e. those with a size of 0 bytes
--max-file-size    | -max     | \<unsigned long\> | infinity            | Skip files larger than the specified number of bytes. <br> This command supports suffixes (e.g. -max 2G)
--min-file-size    | -min     | \<unsigned long\> | 0                   | Skip files smaller than the specified number of bytes. <br> This command supports suffixes (e.g. -min 10k)
--no-interactive   | -N       | -                 | interactive enabled | The program will exit right after processing the paths of the file specified with --directory-file. No interactive console will show up.
--skip-empty-files | -se      | -                 | false               | Skip empty files from indexing/hashing

### Interactive console (mode) commands:

Command       | Short | Arguments                | Description
:-----------: | :---: | :----------------------: | :---------------------------------------------------------------------------------------:
exit          | e     | -                        | Exit the program
crawl         | c     | \<directory path\>       | Crawl a directory with predetermined options <br> (either through command line or using *add-options*)
drop-wrappers | rw    | -                        | Clear all wrappers (file hashes and metadata) from memory
dump-wrappers | dw    | \<file name\>            | Dump the file hashes into a new file which can be loaded in another session
load-wrappers | lw    | \<file name\>            | Load previously dumped wrappers from a file
drop-options  | do    | -                        | Clear out any options for crawling
add-options   | ao    | \<option\> \[option...\] | Add options afterwards; same syntax as on the command line (look above) <br> Old options are **not** discarded, including command line options

## Example

Imagine you have a directory with saved photos in the right order and sorted into subdirectories (`/example/path`).
Now imagine you just removed an old hard drive (`/another/path`) that might contain thousands of these photos with different filenames because your old photo editing software changed them when you imported them to your old computer, and some extra images, which you have never seen before.

Now you have the following problem: **Which ones are duplicates and are taking up expensive hard disk space?**

### Create a hash file

First we will crawl the sorted directory and hash its files.

```
$ java -jar FilesystemOptimizer.jar
Filesystem Optimizer v1.0
(0)$> c /example/path
Entering directory "/example/path"
Analyzed 7/7 entries in "/example/path"
```

Now we can dump every hash (+ size, filename extension) in a file, so we don't have to go through gigabytes of data again if yet another old drive happens to turn up. In the worst case, this file can be a few megabytes, but even then it can only be read in again in fractions of a second.

This step is optional, but you can consider it if
1. The sorted directory is final and therefore does not (frequently) change its content
2. Hashing takes minutes to hours due to slow IO or large file sizes

```
(7)$> dw hashes.wdump
(7)$> e
Bye
```

The number in brackets shows the number of files already hashed and stored in the internal set.

### Load and use

**stderr**:
```
$ java -jar FilesystemOptimizer.jar -ed -ef > no_longer_needed
Filesystem Optimizer v1.0
(0)$> lw hashes.wdump
(7)$> ao -dup -min 5k -max 10M
(7)$> c /another/path                       // also works with Windows paths that use backslashes
Entering directory "/another/path"
```

**stdout**:
```
/another/path/a_redundant_file
...
```

**stderr**:
```
Analyzed 19/19 entries in "/another/path"
(26)$> e
Bye
```

**After a thorough review** of the output file paths, the redundant (or empty) files and directories can be deleted.

#### bash (GNU/Linux)

```
cat no_longer_needed | xargs rm -d
```

#### Windows PowerShell

```
cat no_longer_needed | rm
```
