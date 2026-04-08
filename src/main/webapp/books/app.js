const API_BASE_URL = '/api/books';

async function fetchBooks() {
    try {
        const response = await fetch(API_BASE_URL);
        if (!response.ok) throw new Error('Failed to fetch books');

        const books = await response.json();
        renderTable(books);
    } catch (error) {
        console.error(error);
        alert('Error loading books. ' + error);
    }
}

function renderTable(books) {
    const t_body = document.getElementById('bookTableBody');
    t_body.innerHTML = '';

    books.forEach(book => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td><code>${book.code}</code></td>
            <td>${book.title}</td>
            <td>${book.author}</td>
            <td class="actions">
                <button onclick="showEditForm('${book.code}', '${book.title.replace(/'/g, "\\'")}', '${book.author.replace(/'/g, "\\'")}')">Edit</button>
                <button class="danger" onclick="deleteBook('${book.code}')">Delete</button>
            </td>
        `;
        t_body.appendChild(row);
    });
}

document.getElementById('addBookForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const code = document.getElementById('addCode').value;
    const title = document.getElementById('addTitle').value;
    const author = document.getElementById('addAuthor').value;

    try {
        const response = await fetch(API_BASE_URL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ code, title, author })
        });

        if (!response.ok) throw new Error('Failed to add book');

        document.getElementById('addBookForm').reset();
        fetchBooks();
    } catch (error) {
        console.error(error);
        alert('Error adding book.');
    }
});

async function deleteBook(code) {
    if (!confirm(`Are you sure you want to delete book: ${code}?`)) return;

    try {
        const response = await fetch(`${API_BASE_URL}/${code}`, {
            method: 'DELETE'
        });

        if (!response.ok) throw new Error('Failed to delete book');
        fetchBooks();
    } catch (error) {
        console.error(error);
        alert('Error deleting book.');
    }
}

function showEditForm(code, title, author) {
    document.getElementById('addFormCard').classList.add('hidden');
    document.getElementById('editFormCard').classList.remove('hidden');

    document.getElementById('originalEditCode').value = code; // Keeps track of the URL parameter
    document.getElementById('editCode').value = code;         // The editable code field
    document.getElementById('editTitle').value = title;
    document.getElementById('editAuthor').value = author;
}

function cancelEdit() {
    document.getElementById('editFormCard').classList.add('hidden');
    document.getElementById('addFormCard').classList.remove('hidden');
    document.getElementById('editBookForm').reset();
}

document.getElementById('editBookForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const originalCode = document.getElementById('originalEditCode').value;
    const code = document.getElementById('editCode').value;
    const title = document.getElementById('editTitle').value;
    const author = document.getElementById('editAuthor').value;

    try {
        const response = await fetch(`${API_BASE_URL}/${originalCode}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ code, title, author })
        });

        if (!response.ok) throw new Error('Failed to update book');

        cancelEdit();
        fetchBooks();
    } catch (error) {
        console.error(error);
        alert('Error updating book.');
    }
});

document.addEventListener('DOMContentLoaded', fetchBooks);