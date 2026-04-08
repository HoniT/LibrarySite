const API_BASE_URL = '/api/borrowings'; // Ensure your servlet is mapped to this

// --- Fetch & Display Borrowings ---
async function fetchBorrowings() {
    try {
        const response = await fetch(API_BASE_URL);
        if (!response.ok) throw new Error('Failed to fetch borrowings');

        const borrowings = await response.json();
        renderTable(borrowings);
    } catch (error) {
        console.error(error);
        alert('Error loading borrowings.');
    }
}

function renderTable(borrowings) {
    const tbody = document.getElementById('borrowingTableBody');
    tbody.innerHTML = '';

    borrowings.forEach(borrowing => {
        const row = document.createElement('tr');

        const borrowDate = borrowing.borrow_date ? new Date(borrowing.borrow_date).toLocaleDateString() : 'N/A';
        const returnDate = borrowing.return_date ? new Date(borrowing.return_date).toLocaleDateString() : 'Not Returned';

        // Only show the "Return" button if the book hasn't been returned yet
        let actionsHtml = '';
        if (!borrowing.return_date) {
            actionsHtml = `<button onclick="returnBook('${borrowing.book_code.replace(/'/g, "\\'")}')">Return Book</button>`;
        } else {
            actionsHtml = `<span style="color: gray; font-style: italic;">Returned</span>`;
        }

        row.innerHTML = `
            <td><code>${borrowing.id}</code></td>
            <td>${borrowing.book_code}</td>
            <td>${borrowing.member_id}</td>
            <td>${borrowDate}</td>
            <td>${returnDate}</td>
            <td class="actions">
                ${actionsHtml}
            </td>
        `;
        tbody.appendChild(row);
    });
}

// --- Borrow Book (POST /api/borrowings) ---
document.getElementById('borrowBookForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const bookCode = document.getElementById('borrowBookCode').value;
    const memberId = parseInt(document.getElementById('borrowMemberId').value, 10); // Ensure it's a number

    try {
        const response = await fetch(API_BASE_URL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ bookCode, memberId })
        });

        if (!response.ok) {
            const errorMsg = await response.text();
            throw new Error(errorMsg || 'Failed to borrow book');
        }

        document.getElementById('borrowBookForm').reset();
        fetchBorrowings(); // Refresh list
    } catch (error) {
        console.error(error);
        alert(error.message);
    }
});

// --- Return Book (POST /api/borrowings/return) ---
async function returnBook(bookCode) {
    if (!confirm(`Are you sure you want to mark book '${bookCode}' as returned?`)) return;

    try {
        const response = await fetch(`${API_BASE_URL}/return`, {
            method: 'POST', // Notice this is POST because it maps to the /return path in your doPost
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ bookCode })
        });

        if (!response.ok) {
            const errorMsg = await response.text();
            throw new Error(errorMsg || 'Failed to return book');
        }

        fetchBorrowings(); // Refresh list to show return date
    } catch (error) {
        console.error(error);
        alert(error.message);
    }
}

// Initial Load
document.addEventListener('DOMContentLoaded', fetchBorrowings);