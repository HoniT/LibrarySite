const API_BASE_URL = '/api/members';

async function fetchMembers() {
    try {
        const response = await fetch(API_BASE_URL);
        if (!response.ok) throw new Error('Failed to fetch members');

        const members = await response.json();
        renderTable(members);
    } catch (error) {
        console.error(error);
        alert('Error loading members.');
    }
}

function renderTable(members) {
    const tbody = document.getElementById('memberTableBody');
    tbody.innerHTML = '';

    members.forEach(member => {
        const row = document.createElement('tr');
        // Handling potentially missing join_date based on entity
        const joinDate = member.join_date ? new Date(member.join_date).toLocaleDateString() : 'N/A';

        row.innerHTML = `
            <td><code>${member.id}</code></td>
            <td>${member.name}</td>
            <td>${member.email}</td>
            <td>${joinDate}</td>
            <td class="actions">
                <button onclick="showEditForm(${member.id}, '${member.name.replace(/'/g, "\\'")}', '${member.email.replace(/'/g, "\\'")}')">Edit</button>
                <button class="danger" onclick="deleteMember(${member.id})">Delete</button>
            </td>
        `;
        tbody.appendChild(row);
    });
}

document.getElementById('addMemberForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const name = document.getElementById('addName').value;
    const email = document.getElementById('addEmail').value;

    try {
        const response = await fetch(API_BASE_URL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, email })
        });

        if (!response.ok) {
            const errorMsg = await response.text();
            throw new Error(errorMsg || 'Failed to add member');
        }

        document.getElementById('addMemberForm').reset();
        fetchMembers();
    } catch (error) {
        console.error(error);
        alert(error.message);
    }
});

async function deleteMember(id) {
    if (!confirm(`Are you sure you want to delete member ID: ${id}?`)) return;

    try {
        const response = await fetch(`${API_BASE_URL}/${id}`, {
            method: 'DELETE'
        });

        if (!response.ok) throw new Error('Failed to delete member');
        fetchMembers();
    } catch (error) {
        console.error(error);
        alert('Error deleting member.');
    }
}

function showEditForm(id, name, email) {
    document.getElementById('addFormCard').classList.add('hidden');
    document.getElementById('editFormCard').classList.remove('hidden');

    document.getElementById('editId').value = id;
    document.getElementById('editName').value = name;
    document.getElementById('editEmail').value = email;
}

function cancelEdit() {
    document.getElementById('editFormCard').classList.add('hidden');
    document.getElementById('addFormCard').classList.remove('hidden');
    document.getElementById('editMemberForm').reset();
}

document.getElementById('editMemberForm').addEventListener('submit', async (e) => {
    e.preventDefault();

    const id = document.getElementById('editId').value;
    const name = document.getElementById('editName').value;
    const email = document.getElementById('editEmail').value;

    try {
        const response = await fetch(`${API_BASE_URL}/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ name, email })
        });

        if (!response.ok) {
            const errorMsg = await response.text();
            throw new Error(errorMsg || 'Failed to update member');
        }

        cancelEdit();
        fetchMembers();
    } catch (error) {
        console.error(error);
        alert(error.message);
    }
});

document.addEventListener('DOMContentLoaded', fetchMembers);