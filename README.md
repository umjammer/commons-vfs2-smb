[![Release](https://jitpack.io/v/umjammer/commons-vfs2-smb.svg)](https://jitpack.io/#umjammer/commons-vfs2-smb)
[![Java CI](https://github.com/umjammer/commons-vfs2-smb/actions/workflows/maven.yml/badge.svg)](https://github.com/umjammer/commons-vfs2-smb/actions/workflows/maven.yml)
[![CodeQL](https://github.com/umjammer/commons-vfs2-smb/actions/workflows/codeql-analysis.yml/badge.svg)](https://github.com/umjammer/commons-vfs2-smb/actions/workflows/codeql-analysis.yml)
![Java](https://img.shields.io/badge/Java-8-b07219)
[![Parent](https://img.shields.io/badge/Parent-vavi--apps--fuse-pink)](https://github.com/umjammer/vavi-apps-fuse)

# commons-vfs2-smb

A production tested SMB FileSystemProvider for [Apache commons-vfs2](https://commons.apache.org/proper/commons-vfs/) based on [smbj](https://github.com/hierynomus/smbj).

## Introduction

This project implements required commons-vfs2 interfaces to allow interaction with SMB 2/3 using [Jeroen van Erp](https://github.com/hierynomus)'s [smbj](https://github.com/hierynomus/smbj) implementation.

I've created this library for a project that has been running in production for same time, and the features I've implemented were the ones I needed.

In case you are missing some feature, feel free to [file a bug](https://github.com/mikhasd/commons-vfs2-smb/issues/new) or send a Pull Request.

## Install

* [maven](https://jitpack.io/#umjammer/commons-vfs2-smb)

## Usage

I'm still working on having a proper CI pipeline setup and the library uploaded to Maven Central. Any help is welcome.

```java
VFS.getManager().resolveFile("smb://DOMAIN\\USERNAME:PASSWORD@HOSTNAME:PORT/SHARENAME/PATH");
```
