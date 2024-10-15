function insertData() {
    const date = document.getElementById('date').value;
    const product = document.getElementById('product').value;
    const category = document.getElementById('category').value;
    const quantity = document.getElementById('quantity').value;
    const revenue = document.getElementById('revenue').value;

    const data = {
        date: date,
        product: product,
        category: category,
        quantity: parseInt(quantity),
        revenue: parseFloat(revenue)
    };

    fetch('/api/insert', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(data),
    })
    .then(response => response.text())
    .then(data => {
        alert(data);
        // Clear the form
        document.getElementById('date').value = '';
        document.getElementById('product').value = '';
        document.getElementById('category').value = '';
        document.getElementById('quantity').value = '';
        document.getElementById('revenue').value = '';
    })
    .catch((error) => {
        console.error('Error:', error);
        alert('Error inserting data');
    });
}

function getAverages() {
    fetch('/api/averages')
        .then(response => response.json())
        .then(data => {
            document.getElementById('result').innerHTML = `
                <h2>Average Revenue</h2>
                <p>The average revenue is: $${data[0].avg_revenue.toFixed(2)}</p>
            `;
        })
        .catch(error => {
            console.error('Error:', error);
            document.getElementById('result').innerHTML = '<p>Error fetching data</p>';
        });
}

function getRevenueContribution() {
    fetch('/api/revenue-contribution')
        .then(response => response.json())
        .then(data => {
            let html = '<h2>Revenue Contribution</h2><table border="1"><tr><th>Product</th><th>Revenue</th><th>Percentage</th></tr>';
            data.forEach(item => {
                html += `<tr><td>${item.product}</td><td>$${item.product_revenue.toFixed(2)}</td><td>${item.revenue_percentage.toFixed(2)}%</td></tr>`;
            });
            html += '</table>';
            document.getElementById('result').innerHTML = html;
        })
        .catch(error => {
            console.error('Error:', error);
            document.getElementById('result').innerHTML = '<p>Error fetching data</p>';
        });
}