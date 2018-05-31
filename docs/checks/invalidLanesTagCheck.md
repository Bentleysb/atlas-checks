# Invalid Lanes Tag Check 

This check flags roads that have invalid `lanes` tag values.

Valid values are configurable. The defaults are:  
`1,1.5,2,3,4,5,6,7,8,9,10`

In OSM, generally, lanes values greater than 10 are incorrect, and no lanes values should have special characters with the exception of 1.5. The lanes value 1.5 is valid, due to people often using this value to signify a narrow two lane road.  

Large `lanes` values are often valid when the road is one way, as they can include things such as toll plazas.

#### Live Example

* Is an Edge
* Has a `highway` tag
* Has a `lanes` tag

```java
@Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return Validators.hasValuesFor(object, LanesTag.class)
                && HighwayTag.highwayTag(object).isPresent() && object instanceof Edge
                && !this.isFlagged(((Edge) object).getOsmIdentifier());
    }
```

The valid objects are then tested against the list of valid `lanes` tag values, and flagged if their value is invalid.  
The list of valid values can include exact values and a minimum value for one way roads. 

```java
@Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        if (!this.lanesFilter.test(object)
                        && !((Validators.isOfType(object, OneWayTag.class, OneWayTag.YES)
                                || Validators.isOfType(object, OneWayTag.class, OneWayTag.ONE))
                                && LanesTag.numberOfLanes(object)
                                        .orElse((int) excludeOnewayMinimum - 1) >= excludeOnewayMinimum))
        {
            this.markAsFlagged(((Edge) object).getOsmIdentifier());
            return Optional.of(this.createFlag(object,
                    this.getLocalizedInstruction(0, object.getOsmIdentifier())));
        }
        return Optional.empty();
    }
```

To learn more about the code, please look at the comments in the source code for the check.  
[InvalidLanesTagCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/tag/InvalidLanesTagCheck.java)
=======
>>>>>>> parent of fbc76d5... Finished creating InvalidLanesTagCheck.md, added serial version uid.
=======
 
>>>>>>> parent of 3f2aaa4... Continued creation of InvalidLanesTagCheck.md
