#!/bin/python3
# -*- coding:utf-8 -*-
import os
import sys
import xml.etree.ElementTree as ET

valid_alphabet = set()
valid_allophones = set()

valid_count=0
invalid_count=0

def load_allophones(allophones_file_path):
    allophones = ET.parse(allophones_file_path).getroot()
    for allophone in allophones:
        valid_allophones.add(allophone.get('ph'))

def load_alphabet():
    alphabet_file_path = os.path.join(os.path.dirname(os.path.realpath(__file__)), 'alphabet.txt')
    with open(alphabet_file_path, 'r', encoding='utf-8') as alphabet_file:
        for letter in alphabet_file:
            valid_alphabet.add(letter.strip().lower())

def out_of_alphabet(graphemes):
    graphemes = graphemes.lower()
    return set(graphemes) - valid_alphabet

def get_unknown_allophones(phoneset):
    return set(phoneset) - valid_allophones


if __name__ == '__main__':
    
    f = sys.stdin
    load_alphabet()
    allophones_file_path=sys.argv[1]
    load_allophones(allophones_file_path)
 
    for lexicon_entry in f.read().rstrip().split('\n'):
        graphemes, pos, phonemes, ipa = '','','',''

        # space (nm/f) 's p ee s /'spɛːs/
        graphemes_roe = lexicon_entry.split(' ',1)
        graphemes = graphemes_roe[0] 
        if len(graphemes_roe) > 1:
            if graphemes_roe[1].startswith("("):                
                pos = graphemes_roe[1].split(' ', 1)[0].replace("(","").replace(")","").rstrip()
                phonemes = graphemes_roe[1].split(' ', 1)[1].rsplit(' ', 1)[0].rstrip()
                ipa = graphemes_roe[1].split(' ', 1)[1].rsplit(' ', 1)[1].rstrip()
            else:              
                phonemes = graphemes_roe[1].rsplit(' ', 1)[0].rstrip()
                ipa = graphemes_roe[1].rsplit(' ', 1)[1].rstrip()

            #
            if len(out_of_alphabet(graphemes)) > 0:
                print ("Ignoring because of OOA : %s %s" % (lexicon_entry, out_of_alphabet(graphemes)))
                invalid_count += 1
                continue

            phonemes_array = phonemes.split(' ')
            for i, p in enumerate(phonemes_array):
                p = p.replace(".","")
                p = p.replace("'","")
                phonemes_array[i]=p

            unknown_allophones = get_unknown_allophones(phonemes_array)
            if len(unknown_allophones) > 0:
                print ("Ignoring because of %s unknown allophone : %s - %s" % (len(unknown_allophones), lexicon_entry, unknown_allophones))
                invalid_count += 1
                continue

            valid_count += 1

    print ("Valid Lexicon Entries: %s  Invalid Lexicon Entries: %s" % (valid_count, invalid_count)) 

