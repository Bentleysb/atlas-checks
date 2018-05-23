# Invalid Lanes Tag Check 

This check flags roads that have invalid `lanes` tag values.

Valid values are configurable. The defaults are:  
`1,1.5,2,3,4,5,6,7,8,9,10`

In OSM, generally, lanes values greater than 10 are incorrect, and no lanes values should have special characters with the exception of 1.5. The lanes value 1.5 is valid, due to people often using this value to signify a narrow two lane road.

#### Live Examples

1. The way [id:313769043](https://www.openstreetmap.org/way/313769043) has an invalid `lanes` tag value of `2;1`. `lanes` tag values must be numeric. 
2. The way [id:58693335](https://www.openstreetmap.org/way/58693335) has an invalid `lanes` tag value of `20`. Satellite imagery shows this to a misrepresentation of reality.

#### Code Review

In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines, Nodes & Relations; in our case, we’re are looking at [Edges](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Edge.java).

Our first goal is to validate the incoming Atlas object. Valid features for this check will satisfy the following conditions:

