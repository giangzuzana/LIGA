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
import codecs
import sys
from gz_utils import *   
              
class TreeNode():    
    def __init__(self, cl='', en='', uri=''):
        self.charList   = cl 
        self.entityName = en
        self.entityURI  = uri
        self.childNodes = {}        

    def __str__(self, level=0):
        ret  = '  '*level + repr(self.charList)  
        ret += '\t:'      + repr(self.entityURI) + '\n'
        for k, child in self.childNodes.iteritems():
            ret += child.__str__(level+1)
        return ret
           
    def setValues(self, cl, en, uri):
        self.charList   = cl 
        self.entityName = en
        self.entityURI  = uri
        
    def addChar(self, c):
        self.charList += c            

    def addURI(self, uri):
        if len(self.entityURI) > 0:
            if uri not in self.entityURI.split():
                self.entityURI += ' ' + uri
        else:
            self.entityURI = uri
            
    def isList(self):
        if len(self.entityURI) > 0:
            return True
        return False   

######################
class ListParser(): 
    def getGazetteer(self):
        return self.rootNode  

    def isTurkish(self, string):
        if any((c in set(unicode('ıİşŞğĞüÜöÖçÇ', 'utf-8'))) for c in string):
            return True
        return False                        
    
class ListParserHMT(ListParser):       
    def __init__(self, filename):
        self.rootNode = TreeNode()
        numLine = 0
        numChar = 0
        numNode = 0
        with codecs.open(filename, 'r', 'utf-8') as f:
        # with open(filename) as f:
            print '\nListParser:', filename, 
            for line in f:
                numLine += 1  
                if numLine % 10000 == 0:
                    print '.',
                    
                tokens = line.split('\t')
                uri  = tokens[0]			
                name = ' '.join(tokens[2].strip().split()).lower()
                if len(name) <= 3 or self.isTurkish(name):
                    continue
                
                current = self.rootNode
                for c in name:
                    numChar += 1                    
                    if c not in current.childNodes:
                        numNode += 1
                        current.childNodes[c] = TreeNode(c)  
                    current = current.childNodes[c]
                    
                current.addURI(uri)
                current.entityName = name
        print '\nnumLine=', numLine, '\tnumChar=', numChar, '\tnumNode=', numNode
        return

class ListParserPHT(ListParser):       
    def setNodes(self, current, reading):
        if len(current.charList) > len(reading):
            remain = current.charList[len(reading):]
            newNode = TreeNode(remain, current.entityName, current.entityURI)
            cn = newNode.childNodes
            newNode.childNodes = current.childNodes
            current.childNodes = cn
            current.childNodes[ remain[0] ] = newNode
            current.setValues(reading, '', '')
            return True
        else:
            print 'SetNodes ???'
            return False
        
    def __init__(self, filename):
        self.rootNode = TreeNode()
        numLine = 0
        numChar = 0
        numNode = 0
        numErr  = 0
        with codecs.open(filename, 'r', 'utf-8') as f:
        # with open(filename) as f:
            print '\nListParser:', filename, 
            for line in f:
                numLine += 1  
                if numLine % 10000 == 0:
                    print '.',
                    
                tokens = line.split('\t')
                uri  = ' '.join(tokens[0].strip().split())
                name = ' '.join(tokens[2].strip().split()).lower()
                if len(name) <= 3 or self.isTurkish(name):
                    continue

                current = self.rootNode
                readOut = ''
                reading = ''
                for i, c in enumerate(name):
                    numChar += 1
                    reading  = name[len(readOut):i]  
                    if reading.endswith(current.charList):
                        if len(current.childNodes) == 0 and not current.isList() and current != self.rootNode:
                            current.addChar(c)
                        else:
                            readOut += current.charList
                            reading  = name[len(readOut):i]
                            if c not in current.childNodes:
                                numNode += 1
                                current.childNodes[c] = TreeNode(c)
                            current = current.childNodes[c]
                    else:
                        if not current.charList.startswith(reading+c):
                            if self.setNodes(current, reading):
                                numNode += 1                                
                            readOut += current.charList
                            reading  = name[len(readOut):i]
                            
                            numNode += 1
                            current.childNodes[c] = TreeNode(c)
                            current = current.childNodes[c]
                r = reading + c
                if current.charList.startswith(r) and len(current.charList) > len(r):
                    if self.setNodes(current, r):
                        numNode += 1
                current.addURI(uri)    
                
                if current.entityName == '':
                    current.entityName = name
                elif not current.entityName == name:
                    numErr += 1
                    print "\n\tERR: NAME=", name, '\tREADING=', reading
                    print '\t\tCL=', current.charList, '\tNE=', current.entityName, current.isList()
        print '\nnumErr=', numErr, '\tnumLine=', numLine, '\tnumChar=', numChar, '\tnumNode=', numNode
        return
        
######################                
class TextAnalyzer():
    def __init__(self, rootNode):
        self.rootNode = rootNode
        self.occurNE = {}           # { treeNode: [occur1, ...n] }
        return
        
    def __str__(self, showAll=False):
        ret = '\nocurrences=' + str(len(self.occurNE)) + '\n'
        if showAll:
            for k, v in self.occurNE.iteritems():
                ret += '\n'    + k.entityName.encode('utf-8', 'ignore')                 
                ret += '\t(' + str(len(v)) + ') ' 
                # ret += ' '.join(str(e) for e in v)
                # ret += '\n\t'  + k.entityURI
        return ret
    
    def getOccurences(self):
        return self.occurNE
        
    def checkMatchedEntity(self, current, line, i, posFile):
        if not line[i].isalnum():
            e = current.entityName
            p = posFile + i - len(e)
            if current not in self.occurNE:
                self.occurNE[current] = []
            self.occurNE[current].append(p)    

class TextAnalyzerHMT(TextAnalyzer):   
    def handleFile(self, filename):
        posFile = 0
        numLine = 0
        with codecs.open(filename, 'r', 'utf-8') as f:        
        # with open(filename) as f:
            print '\nTextAnalyzer:', filename, 
            for line in f:
                numLine += 1  
                if numLine % 10000 == 0:
                    print '.',
                    
                line = line.lower()
                current = self.rootNode
                reading = ''
                i = 0
                while i < len(line):
                    c = line[i]
                    i += 1
                    if c == ' ' and current == self.rootNode:
                        continue
                    if c in current.childNodes:
                        reading += c
                        current  = current.childNodes[c]
                        if current.isList():
                            self.checkMatchedEntity(current, line, i, posFile)
                    else:
                        if len(reading) > 0 and ' ' in reading:
                            reading += c
                            i = i - len(reading) + reading.index(' ')
                        current = self.rootNode
                        reading = ''                    
                posFile += len(line)
        return
    
class TextAnalyzerPHT(TextAnalyzer):
    def handleFile(self, filename):
        posFile = 0   
        numLine = 0
        with codecs.open(filename, 'r', 'utf-8') as f:
        # with open(filename) as f:
            print '\nTextAnalyzer:', filename, 
            for line in f:
                numLine += 1  
                if numLine % 10000 == 0:
                    print '.',
                    
                line = line.lower()
                current = self.rootNode
                reading = ''
                onNode  = 0
                i = 0
                while i < len(line):
                    c = line[i]
                    i += 1
                    if c == ' ' and current == self.rootNode:
                        continue
                    
                    cl = current.charList
                    if onNode > 0 and onNode < len(cl) and len(cl) > 1 and c == cl[onNode]:
                        reading += c
                        onNode  += 1
                        if current.isList() and onNode == len(cl):
                            self.checkMatchedEntity(current, line, i, posFile)
                    elif c in current.childNodes:
                        current = current.childNodes[c]
                        cl = current.charList
                        reading += c
                        onNode = 1
                        if current.isList() and len(cl) == 1:
                            self.checkMatchedEntity(current, line, i, posFile)
                            onNode = 0
                    else:
                        if len(reading) > 0 and ' ' in reading:
                            reading += c
                            i = i - len(reading) + reading.index(' ')
                        current = self.rootNode
                        reading = '' 
                        onNode = 0
                posFile += len(line)
        return
        
######################    
def main(argv, start):
    #rootNode = ListParserHMT(argv.gazFilename).getGazetteer()
    #ta = TextAnalyzerHMT(rootNode)
    rootNode = ListParserPHT(argv.gazFilename).getGazetteer()
    ta = TextAnalyzerPHT(rootNode)
    ta.handleFile(argv.inputFilename)
    # print rootNode
    print ta
    print_log('\nRuntime: ' + run_time(start) + '\n', argv.logFilename)   
    
if __name__ == "__main__":
    start   = time.strftime('%Y%m%d-%H%M%S')
    base_fn = os.path.splitext(os.path.basename(__file__))[0] + '_' + start
    
    parser = argparse.ArgumentParser(description='GZ-spark test', epilog='---')
    parser.add_argument("-g", "--gaz",
                        default='data/gazetteer/crosby-gaz.txt',
                        #default='data/gazetteer/org-100000.txt',
                        #default='data/gazetteer/organizations_tripples.txt',
                        dest="gazFilename", help="gazetteer filename", metavar="FILENAME")
    parser.add_argument("-f", "--file",
                        default='data/gazetteer/crosby-input-utf.txt',
                        #default='data/gazetteer/_reuters-concat-2586.xml',
                        dest="inputFilename", help="local log filename", metavar="FILENAME")        
    '''
    parser.add_argument("-o", "--out",
                        default='./out/' + base_fn  + '.out',
                        dest="outputFilename", help="output filename", metavar="FILENAME")
    '''
    parser.add_argument("-l", "--log",
                        default='./out/' + base_fn  + '.log',
                        dest="logFilename", help="local log filename", metavar="FILENAME")
    argv = parser.parse_args()
    if not os.path.exists(argv.gazFilename):
        sys.exit('Error - no such input gazetteer file')
    if not os.path.exists(argv.inputFilename):
        sys.exit('Error - no such input file')    
       
    main(argv, start)
    
