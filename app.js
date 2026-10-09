const imageById = {
  1: 'photo-1568901346375-23c9450c58cd', 2: 'photo-1626700051175-6818013e1d4f',
  3: 'photo-1574071318508-1cdbab80d002', 4: 'photo-1573080496219-bb080dd4f877',
  5: 'photo-1528735602780-2552fd46c7af', 6: 'photo-1585032226651-759b368d7246',
  7: 'photo-1517701604599-bb29b565090c', 8: 'photo-1513558161293-cdaf765edfd7',
  9: 'photo-1556679343-c7306c1976bc', 10: 'photo-1606313564200-e75d5e30476c'
};
const fallbackImages = {
  7:'https://images.unsplash.com/photo-1517701604599-bb29b565090c?auto=format&fit=crop&w=650&q=80',
  8:'https://images.unsplash.com/photo-1513558161293-cdaf765edfd7?auto=format&fit=crop&w=650&q=80'
};
const photo = id => fallbackImages[id] || `https://images.unsplash.com/${imageById[id]}?auto=format&fit=crop&w=650&q=80`;
const menuGrid = document.getElementById('menu-grid');
const cartItems = document.getElementById('cart-items');
const cart = new Map();
let menu = [];
let category = 'All';
let toastTimer;
const money = value => `₹${Number(value).toLocaleString('en-IN', {maximumFractionDigits: 2})}`;

async function loadMenu() {
  try {
    const response = await fetch('/api/menu');
    if (!response.ok) throw new Error('Menu unavailable');
    menu = await response.json();
    renderMenu();
  } catch (error) {
    menuGrid.innerHTML = '<div class="no-results">Could not connect to the Java server. Please run <b>run.bat</b> and refresh this page.</div>';
  }
}
function renderMenu() {
  const query = document.getElementById('search').value.trim().toLowerCase();
  const filtered = menu.filter(item => (category === 'All' || item.category === category) && item.name.toLowerCase().includes(query));
  document.querySelectorAll('.filter').forEach(button => button.classList.toggle('active', button.dataset.category === category));
  if (!filtered.length) { menuGrid.innerHTML = '<div class="no-results">No bites found. Try another search.</div>'; return; }
  menuGrid.innerHTML = filtered.map((item, index) => `<article class="food-card">
    <div class="food-visual" style="--food-bg:${['#f5e7d9','#e9eddb','#f3e3d7','#e9e7f3'][index % 4]}">
      <img src="${photo(item.id)}" alt="${item.name}" loading="lazy" onerror="this.style.display='none';this.parentElement.classList.add('image-fallback');this.parentElement.insertAdjacentHTML('beforeend','<span style=&quot;font-size:48px&quot;>🍽️</span>')">
      ${item.id === 1 || item.id === 3 || item.id === 7 ? '<span class="food-tag">STUDENT FAVOURITE</span>' : ''}
    </div><div class="food-info"><div class="food-category">${item.category}</div><h3>${item.name}</h3><div class="food-bottom"><span class="food-price">${money(item.price)}</span><button class="add-btn" data-add="${item.id}" aria-label="Add ${item.name} to order">+</button></div></div></article>`).join('');
}
function updateCart() {
  const count = [...cart.values()].reduce((sum, qty) => sum + qty, 0);
  document.getElementById('cart-count').textContent = count;
  document.getElementById('cart-count-side').textContent = count;
  const selected = menu.filter(item => cart.has(item.id));
  const total = selected.reduce((sum, item) => sum + item.price * cart.get(item.id), 0);
  document.getElementById('subtotal').textContent = money(total);
  document.getElementById('checkout').disabled = count === 0;
  if (!selected.length) {
    cartItems.innerHTML = '<div class="empty-cart"><span>🥡</span><b>Your cart is hungry.</b><p>Add something delicious to get started.</p></div>';
    return;
  }
  cartItems.innerHTML = selected.map(item => `<div class="cart-row"><div><div class="cart-row-name">${item.name}</div><div class="cart-row-price">${money(item.price * cart.get(item.id))}</div></div><div class="quantity-control"><button data-change="${item.id}" data-step="-1" aria-label="Remove one ${item.name}">−</button><span>${cart.get(item.id)}</span><button data-change="${item.id}" data-step="1" aria-label="Add one ${item.name}">+</button></div></div>`).join('');
}
function showToast(message) {
  const toast = document.getElementById('toast'); toast.textContent = message; toast.classList.add('show');
  clearTimeout(toastTimer); toastTimer = setTimeout(() => toast.classList.remove('show'), 2200);
}
menuGrid.addEventListener('click', event => {
  const button = event.target.closest('[data-add]'); if (!button) return;
  const id = Number(button.dataset.add); const next = (cart.get(id) || 0) + 1;
  if (next > 20) { showToast('Maximum 20 of each item, please.'); return; }
  cart.set(id, next); updateCart(); showToast('Added to your order');
});
cartItems.addEventListener('click', event => {
  const button = event.target.closest('[data-change]'); if (!button) return;
  const id = Number(button.dataset.change); const next = (cart.get(id) || 0) + Number(button.dataset.step);
  if (next < 1) cart.delete(id); else if (next <= 20) cart.set(id, next);
  updateCart();
});
document.getElementById('filters').addEventListener('click', event => {
  const button = event.target.closest('[data-category]'); if (!button) return;
  category = button.dataset.category; renderMenu();
});
document.getElementById('search').addEventListener('input', renderMenu);
document.getElementById('jump-cart').addEventListener('click', () => document.getElementById('cart').scrollIntoView({behavior:'smooth',block:'center'}));
document.getElementById('checkout').addEventListener('click', async () => {
  const button = document.getElementById('checkout'); button.disabled = true; button.innerHTML = 'Placing your order…';
  const cartValue = [...cart.entries()].map(([id, qty]) => `${id}:${qty}`).join(',');
  const customer = document.getElementById('customer-name').value.trim() || 'Guest';
  try {
    const response = await fetch('/api/checkout', {method:'POST',headers:{'Content-Type':'application/x-www-form-urlencoded;charset=UTF-8'},body:new URLSearchParams({cart:cartValue,customer})});
    const result = await response.json();
    if (!response.ok) throw new Error(result.error || 'Could not place your order.');
    document.getElementById('success-message').textContent = `Thanks, ${result.customer}! Your order number is ${result.orderId}.`;
    document.getElementById('receipt-lines').innerHTML = result.items.map(item => `<div class="receipt-line"><span>${item.name} × ${item.quantity}</span><strong>${money(item.amount)}</strong></div>`).join('');
    document.getElementById('receipt-total').textContent = money(result.total);
    document.getElementById('success-modal').hidden = false;
    cart.clear(); updateCart();
  } catch (error) { showToast(error.message); }
  finally { button.innerHTML = 'Place my order <span>→</span>'; button.disabled = cart.size === 0; }
});
document.getElementById('done-btn').addEventListener('click', () => { document.getElementById('success-modal').hidden = true; showToast('Have a lovely break!'); });
document.getElementById('success-modal').addEventListener('click', event => { if (event.target.id === 'success-modal') event.currentTarget.hidden = true; });
loadMenu(); updateCart();
