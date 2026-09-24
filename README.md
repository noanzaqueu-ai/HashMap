# 🗺️ Custom HashMap - From Scratch Implementation in Java

A complete and optimized implementation of a `HashMap` from scratch in Java, including the resize/rehash algorithm inspired by Java 8+'s `HashMap`.

## 🎯 Objective

This project was created for educational purposes to demonstrate how one of the most widely used data structures in computer science works internally. The implementation follows best practices and optimizations used by Java itself, serving as study material for:

- Data structures and algorithms
- Hash functions and collision handling
- Memory management and arrays
- Bitwise operations for performance optimization

## ✨ Features

- ✅ **Basic operations:** `put()`, `get()`, `remove()`, `size()`, `isEmpty()`, `clear()`
- ✅ **Collision handling** with linked lists
- ✅ **Automatic resizing** when the load factor is reached
- ✅ **Optimized hash function** with bit spreading
- ✅ **Null key support** (`null` as a key)
- ✅ **Dynamic capacity** (always powers of 2)
- ✅ **Optimized rehash** without hash recalculation (Java 8+)

## 🚀 How to Use

### Prerequisites

- Java 8 or higher
- Any IDE (IntelliJ, Eclipse, VS Code) or terminal with `javac`

### Compiling and Running

```bash
# Clone the repository
git clone https://github.com/your-username/custom-hashmap.git
cd custom-hashmap

# Compile the code
javac CustomHashMap.java

# Run
java CustomHashMap
