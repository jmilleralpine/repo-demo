package com.alpinenow.artifact.uploader;

public class Main {

    public static void main(String[] args) {

        String localRepo;
        String remoteRepo;
        String artifactToInstall;

        if (args.length == 2) {
            // arg 1 is the remote, arg 2 are the coordinates
        } else if (args.length == 3) {

        } else {
            usage();
        }
    }

    private static void usage() {

    }
}
