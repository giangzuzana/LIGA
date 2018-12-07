# -*- coding: utf-8 -*-
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
# implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
"""
Created on Fri Apr 10 12:49:00 2015
@author: giang nguyen
"""
import argparse
import os
import sys
               
class TreeNode():    
    def __init__(self, ch):
        self.character = ch 
        self.entity = ''
        self.URI = ''
        self.childNodes = {}        

    def __str__(self, level=0):
        ret = '  '*level + repr(self.character) + '\n'
        for k, child in self.childNodes.iteritems():
            ret += child.__str__(level+1)
        return ret
           
    def addURI(self, uri):
        if len(self.URI) > 0:
            if uri not in self.URI.split():
                self.URI += uri
        else:
            self.URI = uri
            
    def isList(self):
        if len(self.URI) > 0:
            return True
        return False   
    
class ListParser():
    @staticmethod    
    def isTurkish(line):
        if any((c in set('ıİşŞğĞüÜöÖçÇ')) for c in line):
            return True
        return False        
        
    def __init__(self, filename):
        self.rootNode = TreeNode('root')
        numLine = 0
        numChar = 0
        numNode = 0
        with open(filename) as f:
            for line in f:
                numLine += 1               
                tokens = line.split('\t')
                uri  = tokens[0]			
                name = ' '.join(tokens[2].strip().split()).lower()
                if len(name) <= 3 or self.isTurkish(name):
                    continue
                # print uri, '\t', name
                
                currentNode = self.rootNode
                for ch in name:
                    numChar += 1                    
                    if ch not in currentNode.childNodes:
                        numNode += 1
                        currentNode.childNodes[ch] = TreeNode(ch)  
                    currentNode = currentNode.childNodes[ch]
                    
                currentNode.addURI(uri)
                currentNode.entity = name
        print '\nnumLine=', numLine, '\tnumChar=', numChar, '\tnumNode=', numNode
        return
        
    def getGazetteer(self):
        return self.rootNode                        

class TextAnalyzer():
    def __init__(self, rootNode):
        self.rootNode = rootNode
        self.occurNE = {}           # { entity: [occur] }
        return
        
    def __str__(self):
        ret = '\n'
        for k, v in self.occurNE.iteritems():
            ret += k + ': ' + str(len(v)) + ': ' + ' '.join(str(e) for e in v) + '\n'
        return ret
    
    def handleFile(self, filename):
        posFile = 0
        currentNode = self.rootNode
        with open(filename) as f:
            for line in f:
                line = line.lower()
                reading = ''
                i = 0
                while i < len(line):
                    ch = line[i]
                    i += 1
                    if ch == ' ' and currentNode == self.rootNode:
                        continue
                    if ch in currentNode.childNodes:
                        reading += ch
                        currentNode = currentNode.childNodes[ch]
                        if currentNode.isList():
                            if not line[i].isalnum():
                                e = currentNode.entity
                                p = posFile + i - len(e)
                                # print 'position: ', p, e
                                if e not in self.occurNE:
                                    self.occurNE[e] = []
                                self.occurNE[e].append(p)
                    else:
                        if len(reading) > 0 and ' ' in reading:
                            reading += ch
                            i = i - len(reading)  + reading.index(' ')
                            # print 'i=', i, line[i:]
                        currentNode = self.rootNode
                        reading = ''                    
            posFile += len(line)
        return
    
def main(argv):
    rootNode = ListParser(argv.gazFilename).getGazetteer()
    #print rootNode
    ta = TextAnalyzer(rootNode)
    ta.handleFile(argv.inputFilename)
    print ta
    
if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='GZ-spark test', epilog='---')
    parser.add_argument("-g", "--gaz",
                        default='data/gazetteer/crosby-gaz.txt',
                        dest="gazFilename", help="gazetteer filename", metavar="FILENAME")
    parser.add_argument("-f", "--file",
                        default='data/gazetteer/crosby-input-utf.txt',
                        dest="inputFilename", help="local log filename", metavar="FILENAME")        
    parser.add_argument("--output",
                        default='./hmt_out',
                        dest="outputFilename", help="output filename", metavar="FILENAME")
    parser.add_argument("--log",
                        default='./hmt_log',
                        dest="logFilename", help="local log filename", metavar="FILENAME")
    argv = parser.parse_args()
    
    if not os.path.exists(argv.gazFilename):
        sys.exit('Error - no such input gazetteer file')
    if not os.path.exists(argv.inputFilename):
        sys.exit('Error - no such input file')
        
    main(argv)
    
