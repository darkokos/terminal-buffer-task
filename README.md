# terminal-buffer-task

A terminal buffer data structure, used for storing and manipulating text by terminal emulators. Written in Kotlin.

## Usage

Java 24+ and Gradle 9.0.0+ are required to run the project.

To build the project, from its root, run:

```sh
./gradlew build
```

To run all the tests, from the root of the project, run:

```sh
./gradlew test
```

## Project structure

### TerminalBuffer

The class which represents the buffer. It maintains two logical parts:

- `screen` - a `MutableList<Line>` with exactly `height` entries. It represents the editable part of the buffer.
- `scrollback` - a `MutableList<Line>` that grows dynamically to `maxScrollback`. It represents the scrollable part of the buffer history.

The position where new text is inserted is tracked via a cursor which has its coordinates (`cursorColumn`, `cursorRow`).
The API for moving the cursor ensures that the cursor is always clamped to the buffer bounds.

### Line

Represents a single line of the buffer. It wraps an `Array<Cell>`. Provides methods for manipulating and displaying the content of a single line.

### Cell

Represents a character that can be stored in the buffer, along with all the attributes (`CellAttributes`) needed to render it.
Encapsulates logic for deducing the width of character needed to render it. This way wide characters such as CJK ideographs, Hangul or Katakana could be rendered.

### CellAttributes

The cell attributes are:

- Foreground colour
- Background colour
- Bold (Boolean)
- Italic (Boolean)
- Underline (Boolean)

## Design elaboration

### Cursor movement API and internal cursor movement

Public cursor API clamps the cursor to the buffer bounds, instead of throwing an exception, if a position out of the buffer bounds were to be provided.

Internally, other methods manage the cursor position and ensure that it is consistent.

### Write and insert semantics

The write method overwrites cells, starting from the current cursor position. It passes to the beginning of the next line upon reaching the end of the current line.
Wide characters whose continuations don't fit until the end of the line are pushed entirely into the next line.

Insert shifts all existing content from the current cursor position rightwards. If this content overflows the current line, it passes into the next line.
Displaced content preserves it's original attributes (via `Cell`).

Both write and insert can expand the `screen` and push the topmost `screen` line into the `scrollback`, if they overflow the last line of `screen`.

### Wide character manipulation

Wide characters are stored via two `Cell`s. The first cell stores the character itself, while the second cell is a continuation.
Whenever any part of a wide character is overwritten, both of its parts are cleared.

### Buffer resizing

Upon decreasing the buffer width, all the existing lines (in screen and scrollback) are truncated (the trailing cells are dropped).

Upon increasing the buffer width, all the existing lines are padded with empty cells.

Upon decreasing the buffer height, excess lines are pushed to the scrollback.

Upon increasing the buffer height, lines are retrieved from the scrollback, and, if necessary, the screen is padded with empty lines from the top.

## Trade-offs

### Representing empty cells with the space character

Internally, an empty `Cell` wraps a space character.
While this seemed like a good idea, initially, because the empty characters are rendered as spaces in the terminal, it has proven to be better to separate an empty cell from a cell representing a space character.
This is because the space characters are explicitly added into the buffer, while empty characters should populate all the unpopulated space in the buffer, by default.

## Improvements

### Line repopulation instead of truncation upon buffer resize

The most obvious improvement is repopulating all lines upon buffer shrinking, instead of truncating, as truncating could potentially lead to the loss of buffer content.

### Better wide character deduction

Wide `Cell`s currently only support a subset of wide characters, and their width is deduced by matching them against wide character codes.
These codes are incomplete, but are sufficient for a solution that doesn't use any external libraries.

### Separating empty cells from cells wrapping a space character

Explained in the trade-offs section.