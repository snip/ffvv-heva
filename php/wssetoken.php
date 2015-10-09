#!/usr/bin/env php
<?php
/*
Based on code from: https://gist.github.com/borisguery/3792855
2015-10-09 - Ubdated by Sébastien Chaumontet to comply with Heva FFVV implementation as described on http://www.xml.com/pub/a/2003/12/17/dive.html
*/

function wsse_header($username, $password) {
    #$nonce = hash_hmac('sha512', uniqid(null, true), uniqid(), true);
    $nonce = hash_hmac('sha1', uniqid(null, true), uniqid(), false);
    $created = new DateTime('now', new DateTimezone('UTC'));
    $created = $created->format(DateTime::ISO8601);
    $digest = sha1($nonce.$created.$password, true);

    return sprintf(
        'X-WSSE: UsernameToken Username="%s", PasswordDigest="%s", Nonce="%s", Created="%s"',
        $username,
        base64_encode($digest),
        $nonce,
        $created
    );
}

if (3 === $argc) {
    printf("%s", wsse_header($argv[1], $argv[2]));
    exit(0);
} else {
    printf("Usage: %s [username] [password]\n", ltrim($argv[0], './'));
    exit(1);
}

/**
 * Set up:
 * -------
 * chmod +x wssetoken.php
 *
 * Usage:
 * ------
 * ./wssetoken.php guery.b@gmail.com S3cЯ3tS3cЯɘT
 *
 * Results in:
 * -----------
 * X-WSSE: UsernameToken Username="guery.b@gmail.com", PasswordDigest="hTLPsum05zH16kR8SnQ7wrhqunA=", Nonce="179eb36d58dfe2b3f3d5be038e1d2d6448acf66f", Created="2015-10-09T19:50:03+0000"
 *
 * Usage within curl:
 * ------------------
 * curl -v -H"$(./wssetoken.php guery.b@gmail.com S3cЯ3tS3cЯɘT)" http://example.com/api/secured/resource
 */
