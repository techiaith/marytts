#!/bin/python
# -*- coding:utf-8 -*-

import sys
from string import whitespace

atom_end = set('()"\'') | set(whitespace)

phone_set = set()

def festival_2_artefacts(festival_lts_text):
    #
    # received as 
    # ("<word>" <pos> <phonemes>)
    #
    fields = festival_lts_text.split(' ', 2)
    if len(fields) == 3:
        word = fields[0].replace('(', '').replace('"', '')
        language = 'cy'
        if word.endswith('_en'):
            language = 'en'
        word = word.replace("_cy","").replace("_en","")
        pos = fields[1]
        phonemes = fields[2].rstrip()[:-1]

        return word, language, pos, phonemes

    else:
        return '', '', '', ''


def festival_decode_transliteration(word):

    # circumflex
    word = word.replace("a+", u"\u00E2") #"â")
    word = word.replace("e+", u"\u00EA") #"ê")
    word = word.replace("i+", u"\u00EE")
    word = word.replace("o+", u"\u00F4")
    word = word.replace("u+", u"\u00FB")
    word = word.replace("w+", u"\u0175")
    word = word.replace("y+", u"\u0177")

    # accent
    word = word.replace("a/", u"\u00E1") #"â")
    word = word.replace("e/", u"\u00E9") #"ê")
    word = word.replace("i/", u"\u00ED")
    word = word.replace("o/", u"\u00F3")
    word = word.replace("u/", u"\u00FA")

    # umlaud
    word = word.replace("a%", u"\u00E4") #"â")
    word = word.replace("e%", u"\u00EB") #"ê")
    word = word.replace("i%", u"\u00EF")
    word = word.replace("o%", u"\u00F6")
    word = word.replace("u%", u"\u00FC")

    return word


def parse_festival_phonemes(sexp):
    stack, i, length = [[]], 0, len(sexp)
    while i < length:
        c = sexp[i]
        #print (c, stack)
        reading = type(stack[-1])
        if reading == list:
            if   c == '(': stack.append([])
            elif c == ')': 
                stack[-2].append(stack.pop())
                if stack[-1][0] == ('quote',): stack[-2].append(stack.pop())
            elif c == '"': stack.append('')
            elif c == "'": stack.append([('quote',)])
            elif c in whitespace: pass
            else: stack.append((c,))
        elif reading == str:
            if   c == '"': 
                stack[-2].append(stack.pop())
                if stack[-1][0] == ('quote',): stack[-2].append(stack.pop())
            elif c == '\\': 
                i += 1
                stack[-1] += sexp[i]
            else: stack[-1] += c
        elif reading == tuple:
            if c in atom_end:
                atom = stack.pop()
                if atom[0][0].isdigit(): stack[-1].append(eval(atom[0]))
                else: stack[-1].append(atom)
                if stack[-1][0] == ('quote',): stack[-2].append(stack.pop())
                continue
            else: stack[-1] = ((stack[-1][0] + c),)
        i += 1

    return stack.pop()


if __name__ == '__main__':

    f = sys.stdin

    phones_file = open('phone_set','w') 

    for lexicon_entry in f.read().rstrip().split('\n'):
	
        headword, language, pos, phones = festival_2_artefacts(lexicon_entry)
        headword = festival_decode_transliteration(headword)
        headword = headword.replace("'","")

        if not headword.isalpha():
            continue

        if len(phones) > 0:
            tree = parse_festival_phonemes(phones)
            marytts = ''
            for syllables in tree:
                for syllable in syllables:
                    hasEmphasis = syllable[1] == 1
                    phones = syllable[0]
                    phones_list = []

                    if hasEmphasis:
                        phones_list.append("'")

                    for phone in phones:
                        phones_list.append(phone[0])

                    #if hasEmphasis:	
                        #if len(phones_list) == 3:
                        #    phones_list[1] = "'" + phones_list[1] 
                        #else:
                        #    phones_list[-1] = "'" + phones_list[-1] 

                    if len(marytts) > 0:
                        marytts = marytts + ' - ' + ' '.join(phones_list)
                    else:
                        marytts = ' '.join(phones_list)

                    for unique_phone in phones_list:
                        phone_set.add(unique_phone)

            print ("{0}|{1}".format(headword, marytts.strip()))

    for unique_phone in sorted(phone_set):
        phones_file.write("%s\n" % unique_phone)

