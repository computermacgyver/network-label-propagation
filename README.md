Multithreaded Label Propagation Community Detection
====================

This code performs the community detection using the label propagation method published by [Raghavan, et al.](http://arxiv.org/abs/0709.2938). I wrote this Java implementation to use multiple threads. The main class, [LabelPropagation.java](src/us/hale/scott/networks/community/LabelPropagation.java), gives further details on the input expected and output produced.

This code was used to detect community structures in a network of Twitter mentions and retweets, and the results published in a recent article on [Global Connectivity and Multilinguals in the Twitter Network](http://www.scotthale.net/pubs/?chi2014).

If you use this code in support of an academic publication, please cite the original paper as well as:
   
    Hale, S. A. (2014) Global Connectivity and Multilinguals in the Twitter Network. 
    In Proceedings of the 2014 ACM Annual Conference on Human Factors in Computing Systems, 
    ACM (Montreal, Canada).

  
This code is released under the [GPLv2 license](http://www.gnu.org/licenses/gpl-2.0.html). Please [contact me](http://www.scotthale.net/blog/?page_id=9) if you wish to use the code in ways that the GPLv2 license does not permit.

More details, related code, and the original academic paper using this code is available at http://www.scotthale.net/pubs/?chi2014 .
