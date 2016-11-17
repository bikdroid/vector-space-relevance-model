# vector-space-relevance-model
Implementation of vector space relevance computation technique using cosine similarity between query vector and document vectors -- Information Retrieval 

RelevanceEngine.java -- Includes functionalitities to compute the query and document vectors for the terms (after lemmatization) 
                        with weight scores.
                        Okapi and MaxTF term weight formulae have been utilized for the computation and study. Okapi generally has a better
                        performance.
                        
                        
                        
Indexer.java        -- Functionalities for computing the lemmatized term index and the Stem indexes, the dictionaries and the compression
                        and storage into file system. 

Compression.java    -- The compression algorithms, K Block coding for K=8 and Front Coding (usage of Trie) to achieve compression of the dictionary
                        terms. Gamma and Delta compression used for the postings lists and document statistics like maxtf, doclen
                        and term-frequencies. An approximate 70% reduction in indexed files size was observed using these techniques

