# Scalar values

## Basic scalars

A scalar value is a singular value, in the simplest case this is a string.

```mdd
this is a scalar value
```

```expected
this is a scalar value
```

The parser will initially parse everything as a string, even if it appears to be a number or a boolean or...

```mdd
42
```

```expected
42
```

## Multilines

Anything that follows a value is considered a multiline if it is at the same level (or deeper) and does not start a new structural block, so this is considered one long scalar value:

```mdd
This is a sentence
with a linefeed in it
another linefeed
     and some leading spaces!
and some trailing content as well
```

```expected
This is a sentence
with a linefeed in it
another linefeed
     and some leading spaces!
and some trailing content as well
```

## Scalar collections

A basic scalar collection:

```mdd
- 1
- 2
- 3
```

Should return a basic collection:

```json
["1", "2", "3"]
```

## Scalars in objects

By prepending a scalar with a key, it becomes a named scalar which needs to reside in an object:

```mdd
value: test
```

```json
{"value": "test"}
```

When we have scalar values in a collection, it will look for an element with the given name in the parent, make sure it is a collection and add it to that.

```mdd
- name: john
- age: 30
```

Expected result:

```json
{
	"name": ["john"], 
	"age": ["30"]
}
```

If you add items in a different order, it will be parsed correctly but the original order will be gone so can not be reconstructed based on the parsed data:

```mdd
- name: john
- age: 30
- name: jim
- age: 40
```

Expected result:

```json
{
	"name": ["john", "jim"], 
	"age": ["30", "40"]
}
```

## Combined example

We can create an object and a list with scalars. Each multiline must have at least the same depth as the object it belongs to:

```mdd
reason:
* comment: this is a comment
	that spans multiple lines!
* something:
	* comment: that has a comment
		that features multiple lines!

list:
- This is the first
	test
- this is the second
		test
		
value: multiline
in a text

value2: multiline
in another text
```

When we parse this, it yields the following (expressed in JSON):

```json
{
	"reason": {
		"comment": "this is a comment\nthat spans multiple lines!", 
		"something": {
			"comment": "that has a comment\nthat features multiple lines!"
		}
	}, 
	"list": [ "This is the first\ntest", "this is the second\n\ttest"], 
	"value": "multiline\nin a text", 
	"value2": "multiline\nin another text"
}
```