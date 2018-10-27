# GDTM - Graph-based Dynamic Topic Model

The software for the algorithm presented in the following paper:
 - To be added [PDF](https://www.kth.se/profile/ghoorian)


## Description
GDTM is a single-pass DTM approach that combines a context-rich and incremental feature representation model, called *Random Indexing (RI)* with a novel online *graph partitioning* algorithm to address scalability and dynamicity in topic modeling over short texts. In addition, GDTM uses a rich language modeling approach based on the *Skip-gram* technique to account for sparsity.

## Usage
``` bash
#Synopsis
java -jar gdtm \delta \alpha \gamma
```

#Params:
- \delta # Function words adjustment parameter {value >= 1}
- &#945; # Partition expansion threshold {value = [0 ... 1]}
- \gamma # Function word elimination threshold {value = [0 ... 1]}

# Arbitrary Parameters
* RI_Parameters
- dim
- noz
- win
- pi
- mwt

* KG_Parameters
- skip
- saving_step
- input_dir
- model_dir
- documents_file
- theta_file

## Protocol
![alt text](https://github.com/kambizG/gdtm/blob/master/img/protocol.png "The protocol of the algorithm.")

## Contributors
1. [Kambiz Ghoorchian](https://www.kth.se/profile/ghoorian)
2. [Magnus Sahlgren](https://www.sics.se/people/magnus-sahlgren)
3. [Magnus Boman](https://www.kth.se/profile/mab)

## Acknowledgement
