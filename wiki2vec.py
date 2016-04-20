#! /usr/bin/python

# Copyright 2015 fnoorala.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
from gensim.models import Word2Vec
import gensim

class wiki2vec:
    def __init__(self,model):
        self.model=Word2Vec.load(model)

    def out(self):
        print(self.model.most_similar(positive=['DBPEDIA_ID/paris'],topn=20))
