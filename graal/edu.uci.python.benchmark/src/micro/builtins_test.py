# test builtin abs
x = abs(10)
print(x)

x = abs(10.25)
print(x)

x = abs(1 + 2j)
print(x)

# test builtin complex
x = complex()
print(x)

x = complex(2)
print(x)

x = complex(2,3)
print(x)

# test builtin len
value = "hello"
print(len(value))

value = (100, 200, 300)
print(len(value))

value = ['a', 'b', 'c', 'd']
print(len(value))

value = {'id' : 17, 'name' : "gulfem"}
print(len(value))


# test builtin range
print(list(range(10))) # [0, 1, 2, 3, 4, 5, 6, 7, 8, 9]

print(list(range(1, 11))) # [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]

print(list(range(0, 30, 5))) # [0, 5, 10, 15, 20, 25]
     
     
# test builtin iter and next
str = "gulfem"
it = iter(str)
print it.next()
print it.next()