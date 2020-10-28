#!/bin/python3
# -*- coding:utf-8 -*-
import os
import sys


bangor_dict = []

def process_bangor_dict_entry(entry):
    graphemes, pos, phonemes, ipa = '','','',''

    # space (nm/f) 's p ee s /'spɛːs/
    graphemes_roe = entry.split(' ',1)
    graphemes = graphemes_roe[0]

    if len(graphemes_roe) > 1:

        if graphemes_roe[1].startswith("("):
            pos = graphemes_roe[1].split(' ', 1)[0].replace("(","").replace(")","").rstrip()
            phonemes = graphemes_roe[1].split(' ', 1)[1].rsplit(' ', 1)[0].rstrip()
            ipa = graphemes_roe[1].split(' ', 1)[1].rsplit(' ', 1)[1].rstrip()
        else:
            phonemes = graphemes_roe[1].rsplit(' ', 1)[0].rstrip()
            ipa = graphemes_roe[1].rsplit(' ', 1)[1].rstrip()

    return graphemes, pos, phonemes, ipa


def get_graphemes(dict_file_path):
    if len(bangor_dict)==0:
        load_file(dict_file_path)
    graphemes_set = set()
    for graphemes, pos, phonemes, ipa in bangor_dict:
        graphemes_set.add(graphemes)
    return graphemes_set


def load_file(dict_file_path):
    with open(dict_file_path, 'r', encoding='utf-8') as dict_file:
        for entry in dict_file:
            bangor_dict.append(process_bangor_dict_entry(entry))

 
