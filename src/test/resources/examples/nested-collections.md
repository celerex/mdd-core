# Nested collections

Nested collections are interesting in certain situations though they rarely appear in enterprise data sets.
Only the absolute depth of the dash matters in determining the collection depth.

To force the system to start a new collection rather than appending to a previous collection, empty dashes can be inserted to reset the level.

## A simple matrix

A basic 2x3 matrix. The empty entry is to reset the array to a certain depth. If you add whitespace to the entry, it will be regarded as an entry of whitespace.

```mdd
-
	- test1
	- test2
	- test3
-
	- test2
	- test3
	- test4
```

```json
[
	["test1", "test2", "test3"], 
	["test2", "test3", "test4"]
]
```

Because only the absolute depth counts, this is equivalent to:

```mdd
	- test1
	- test2
	- test3
-
	- test2
	- test3
	- test4
```

```json
[
	["test1", "test2", "test3"], 
	["test2", "test3", "test4"]
]
```

## Deeper nesting

By simply shifting everything one tab to the right, we get a collection with deeper nesting:

```mdd
	-
		- test1
		- test2
		- test3
	-
		- test2
		- test3
		- test4
```

```json
[[
	["test1", "test2", "test3"], 
	["test2", "test3", "test4"]
]]
```

## Combinations

You can mix and match elements, note that in the example below the second "empty" dash actually has whitespace that is counted as a value.

```mdd
- test-begin
-
	- test1
	- test2
	- test3
-
	- test2
	- test3
	- test4
-    
	- test3
	- test4
- test-end
	- test5
		- test6
			with multiline
	- test7
```

As JSON:

```json
[
	"test-begin", 
	["test1", "test2", "test3"],
	["test2", "test3", "test4"], 
	"   ", 
	["test3", "test4"], 
	"test-end", 
	[
		"test5", 
		["test6\nwith multiline"], 
		"test7"
	]
]
```

## Complex nesting

You can also generate more complex nested collections. As you can see in the example, it is the absolute depth of the list marker that counts, not if all list markers for every depth actually exist.

```mdd
-
	-
		-
			- test1
			- test2
			- test3
	- 
		-
			- test4
			- test5
			- test6
	-
			- test7
			- test8
				and more!
			- test9
	-
				- test10
					- test11
						with additional content
				- test12
					- test13
```

This generates:

```json
[
	[
		[["test1", "test2", "test3"]], 
		[["test4", "test5", "test6"]], 
		[["test7", "test8\nand more!", "test9"]],
		[[["test10", ["test11\nwith additional content"], "test12", ["test13"]]]]]
	]
]
```

Because only the absolute depth counts, you can quickly create a deeply nested array:

```mdd
					- test1
					- test2
```

This actually becomes

```json
[[[[[["test1", "test2"]]]]]]
```