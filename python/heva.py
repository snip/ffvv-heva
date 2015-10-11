# -*- coding: utf-8 -*-
"""
Created on Sun Oct 11 10:08:18 2015

@author: Thierry D.
"""

import random, hashlib, datetime, base64, requests

def get_nonce(length=8):
    """génération d'une chaîne hexadécimale aléatoire"""
    return ''.join(random.choice('0123456789abcdef') for n in range(length))

def wsse_header(username, password):
    """ génération d'un en-tête pour connexion WSSE"""

    nonce = get_nonce(32)
    created = datetime.datetime.utcnow().strftime('%Y-%m-%dT%H:%M:%SZ')

    m = hashlib.sha1()
    m.update((nonce + created + password).encode('utf-8'))
    digest = base64.b64encode(m.digest()).decode('utf-8')
    s = 'UsernameToken Username="{0}", PasswordDigest="{1}", Nonce="{2}", Created="{3}"' \
        .format(username, digest, nonce, created) 
    return  s

def get_heva(username, password, url):
    hd = {'Content-Type': 'application/json', \
        'Authorization' : 'WSSE profile="UsernameToken"', \
        'X-WSSE' : wsse_header(username,password)}
    print(hd)
    print
    r = requests.get(url, timeout=5000,  headers = hd)
    print("STATUS : ", r.status_code)
    print(r.text)

get_heva("myWsseLogin", "myWssePassword", "http://api.licences.ffvv.stadline.com/persons")

