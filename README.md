# GDTM - Graph-based Dynamic Topic Model

The software for the algorithm presented in the following paper:
 - To be added [PDF](https://www.kth.se/profile/ghoorian)

## Description
GDTM is a single-pass DTM approach that combines a context-rich and incremental feature representation model, called *Random Indexing (RI)* with a novel online *graph partitioning* algorithm to address scalability and dynamicity in topic modeling over short texts. In addition, GDTM uses a rich language modeling approach based on the *Skip-gram* technique to account for sparsity.

## Usage

``` bash
#Synopsis
java -jar gdtm δ α γ

#Params:
- δ # Function words adjustment parameter {value >= 1}
- α # Partition expansion threshold {value = [0...1]}
- γ # Function word elimination threshold {value = [0...1]}
```

Following is a list of arbitrary parameters to costumize or enhance the performance relative to the volume of the stream.

* RI Params
> - -dim: the dimension of the vector. {value >= 2}. default = 2000
> - -noz: the number of non zero elemtns. {value = [1...dim]}. default = 8
> - -win: the size of the moving window to construct the contex structures. default = 2 
> - -mwt: RI vectors pruning parameter. {value = [0...1]}. default = 0.3

* See also
> - -skip: Skip-gram value. {1 = bigram, 2 = 1-skip-bigram, 3 = 2-skip-bigram, ...}
> - -SN (snapshot): the algorithm will take a snapshot of the partitioned documents and clean the momry.
> - -intput: the input can be set arbitrarily.
> - -output: the output can be set arbitrarily

### Input Data Format
<pre>
Document<sub>1</sub>
Document<sub>2</sub>
...
Document<sub>n</sub>
</pre>

### Output
<pre>
T<sub>1</sub>:L<sub>11</sub>  T<sub>2</sub>:L<sub>12</sub>  ...  T<sub>m</sub>:L<sub>1m</sub>
T<sub>1</sub>:L<sub>21</sub>  T<sub>2</sub>:L<sub>22</sub>  ...  T<sub>m</sub>:L<sub>2m</sub>
...
T<sub>1</sub>:L<sub>n1</sub>  T<sub>2</sub>:L<sub>n2</sub>  ...  T<sub>m</sub>:L<sub>nm</sub>
</pre>

Where T<sub>i</sub> indicates the topic number *i* and L<sub>ji</sub> indicates the corresponding likelihood of the topic *i* for the document *j*.

## Protocol
![alt text](https://github.com/kambizG/gdtm/blob/master/img/protocol.png "The protocol of the algorithm.")

## Contributors
1. [Kambiz Ghoorchian](https://www.kth.se/profile/ghoorian)
2. [Magnus Sahlgren](https://www.sics.se/people/magnus-sahlgren)
3. [Magnus Boman](https://www.kth.se/profile/mab)

## Acknowledgement
