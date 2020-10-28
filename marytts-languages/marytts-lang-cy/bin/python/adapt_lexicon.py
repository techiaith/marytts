#!/bin/python3
# -*- coding:utf-8 -*-
import os
import sys
import xml.etree.ElementTree as ET

valid_alphabet = set()
valid_allophones = set()


def load_allophones(allophones_file_path):
    allophones = ET.parse(allophones_file_path).getroot()
    valid_allophones.add('.')
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


def unknown_allophones(phoneset):
    return set(phoneset) - valid_allophones


if __name__ == '__main__':

    f = sys.stdin
    load_alphabet()
    allophones_file_path = sys.argv[1]
    load_allophones(allophones_file_path)

    for lexicon_entry in f.read().rstrip().split('\n'):
        graphemes, pos, phonemes, ipa = '','','',''

        # space (nm/f) 's p ee s /'spɛːs/
        graphemes_roe = lexicon_entry.split(' ',1)
        graphemes = graphemes_roe[0] 
        if len(graphemes_roe) < 2:
            continue

        if graphemes_roe[1].startswith("("):                
            pos = graphemes_roe[1].split(' ', 1)[0].replace("(","").replace(")","").rstrip()
            phonemes = graphemes_roe[1].split(' ', 1)[1].rsplit(' ', 1)[0].rstrip()
            ipa = graphemes_roe[1].split(' ', 1)[1].rsplit(' ', 1)[1].rstrip()
        else:              
            phonemes = graphemes_roe[1].rsplit(' ', 1)[0].rstrip()
            ipa = graphemes_roe[1].rsplit(' ', 1)[1].rstrip()

        if len(out_of_alphabet(graphemes)) > 0:
            #print ("Ignoring because of OOA : %s %s " % (lexicon_entry, out_of_alphabet(graphemes)))
            continue

        phonemes_array = phonemes.split(' ')
        new_phonemes_array = []
        for i, p in enumerate(phonemes_array):
            p = p.replace("'","") 
            if not p=="-":
                new_phonemes_array.append(p)

        if len(unknown_allophones(new_phonemes_array)) > 0:
            #print ("Ignoring because of unknown allophone : %s %s" % (lexicon_entry, unknown_allophones(new_phonemes_array)))
            continue

        phonemes = ' '.join(new_phonemes_array)
        print ("{0}|{1}".format(graphemes.rstrip(), phonemes.rstrip()))


