# Pointless Point Check

This check flags nodes that are not connected to anything and have only pointless tags.

Pointless tags are ?

#### Live Examples

1. The point [123]() is pointless because 

#### Code Review

In [Atlas](https://github.com/osmlab/atlas), OSM elements are represented as Edges, Points, Lines, Nodes & Relations; in our case, we’re are looking at [Points](https://github.com/osmlab/atlas/blob/dev/src/main/java/org/openstreetmap/atlas/geography/atlas/items/Point.java).

A configurable filter is used to define what tags are pointless for Points.

The default values are:

* `created_by->*` 
* `fixme->*`

```java
public PointlessPointCheck(final Configuration configuration)
    {
        super(configuration);
        this.pointlessTagsFilter = (TaggableFilter) configurationValue(configuration,
                "pointless_tags.filter", POINTLESS_TAGS_FILTER_DEFAULT,
                value -> new TaggableFilter(value.toString()));
    }
```

The object of the Atlas are filtered for testing by the following:

* Must be a Point
* Must contain at least one value in the pointless tags filter

```java
@Override
    public boolean validCheckForObject(final AtlasObject object)
    {
        return object instanceof Point && pointlessTagsFilter.test(object);
    }
```

Finally, the filtered objects are tested to see if they contain any non-pointless tags. If they do not, they are flagged.

```java
@Override
    protected Optional<CheckFlag> flag(final AtlasObject object)
    {
        final Map<String, String> tags = object.getOsmTags();
        for (final String tagKey : tags.keySet())
        {
            final Tag tagPair = new Tag(tagKey, tags.get(tagKey));
            if (!pointlessTagsFilter.test(new TagMap(Collections.singletonList(tagPair))))
            {
                return Optional.empty();
            }
        }
        return Optional.of(this.createFlag(object,
                this.getLocalizedInstruction(0, object.getOsmIdentifier())));
    }
```

To learn more about the code, please look at the comments in the source code for the check.  
[PointlessPointCheck](../../src/main/java/org/openstreetmap/atlas/checks/validation/tag/PointlessPointCheck.java)