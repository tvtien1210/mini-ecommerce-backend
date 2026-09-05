document.addEventListener("DOMContentLoaded", async function () {

    // Load Cart khi mở trang
    await loadCart();

    // Đăng ký sự kiện cho các button trong Cart
    setupCartEvents();


});


// LOAD CART

async function loadCart() {

    try {

        console.log("Loading cart ...");


        // Gọi API lấy Cart của User hiện tại
        const response =
            await apiFetch("/api/cart/my");


        // apiFetch có thể redirect sang /login
        // hoặc không trả về response
        if (!response) {
            return;
        }


        //Đồng thời trả vể response ở dưới
        console.log("Response", response);



        // ==========================================
        // USER CHƯA ĐĂNG NHẬP
        // ==========================================

        if (response.status === 401) {

            renderLoginRequired();

            return;
        }


        // ==========================================
        // API ERROR
        // ==========================================

        if (!response.ok) {

            throw new Error(
                "Failed to load cart"
            );
        }


        // ==========================================
        // GET CART
        // ==========================================

        const cart =
            await response.json();


        console.log("Cart:", cart);

        //Update cart badge
        updateCartBadge(cart)

        // Render Cart
        renderCart(cart);


    } catch (error) {

        console.error(
            "Failed to load cart:",
            error
        );

    }
}

// ==================================================
// LOGIN REQUIRED
// ==================================================

function renderLoginRequired() {

    const cartContainer =
        document.getElementById("cartContainer");

    const cartSummary =
        document.getElementById("cartSummary");


    // Xóa nội dung cũ
    cartContainer.innerHTML = "";
    cartSummary.innerHTML = "";


    // Hiển thị thông báo yêu cầu đăng nhập
   cartContainer.innerHTML = `
       <div class="d-flex justify-content-center align-items-center py-5">

           <div class="text-center p-5 rounded-4 border border-secondary shadow-lg"
                style="
                   max-width: 500px;
                   width: 100%;
                   background: linear-gradient(
                       145deg,
                       #151515,
                       #1f1f1f
                   );
                ">

               <!-- Cart Icon -->
               <div class="mb-4">

                   <div class="d-inline-flex justify-content-center align-items-center rounded-circle"
                        style="
                           width: 100px;
                           height: 100px;
                           background: rgba(25, 135, 84, 0.15);
                           border: 1px solid rgba(25, 135, 84, 0.3);
                        ">

                       <i class="bi bi-cart3"
                          style="
                               font-size: 3.5rem;
                               color: #20c997;
                          ">
                       </i>

                   </div>

               </div>


               <!-- Title -->
               <h3 class="text-light fw-bold mb-3">
                   Your cart is waiting! 🛒
               </h3>


               <!-- Description -->
               <p class="text-secondary mb-4">
                   Looks like you haven't logged in yet.
                   <br>
                   Login to start adding your favorite products!
               </p>


               <!-- Login Button -->
               <a
                   href="/login"
                   class="btn btn-success px-4 py-2 rounded-pill fw-semibold">

                   <i class="bi bi-box-arrow-in-right me-2"></i>

                   Login to Continue

               </a>


               <!-- Continue Shopping -->
               <div class="mt-4">

                   <a
                       href="/products"
                       class="text-secondary text-decoration-none">

                       <i class="bi bi-arrow-left me-2"></i>

                       Continue Shopping

                   </a>

               </div>

           </div>

       </div>
   `;
}


// RENDER CART
function renderCart(cart) {

    const cartContainer =
        document.getElementById("cartContainer");

    const cartSummary =
        document.getElementById("cartSummary");


    // Xóa nội dung cũ
    cartContainer.innerHTML = "";
    cartSummary.innerHTML = "";


    console.log("Cart object: ", cart);


    // EMPTY CART BLOCK
    if (
        !cart.cartItems ||
        cart.cartItems.length === 0
    ) {

        cartContainer.innerHTML = `
            <div class="text-center py-5">

                <i class="bi bi-cart-x display-1 text-secondary"></i>

                <h4 class="mt-3 text-light">
                    Your cart is empty
                </h4>

                <a
                    href="/products"
                    class="btn btn-success mt-3">

                    <i class="bi bi-bag me-2"></i>

                    Continue Shopping

                </a>

            </div>
        `;

        return;
    }


    // RENDER CART ITEMS BLOCK
    const itemsHtml =
        cart.cartItems.map(function (item) {

            // Tính subtotal của từng Cart Item
            const subtotal =
                item.price * item.quantity;


            return `
                <div class="card bg-dark text-light border-secondary mb-3">

                    <div class="card-body">

                        <div class="row align-items-center g-3">


                           <!-- Product Image & Name -->

                           <div class="col-12 col-md-4">

                               <div class="d-flex align-items-center gap-3">

                                   <img
                                       src="${item.imageUrl}"
                                       alt="${item.productName}"
                                       class="rounded"
                                       style="width: 70px; height: 70px; object-fit: cover;">

                                   <h5 class="mb-0">
                                       ${item.productName}
                                   </h5>

                               </div>

                           </div>


                            <!-- Product Price -->

                            <div class="col-6 col-md-2">

                                <div class="text-secondary small">
                                    Price
                                </div>

                                <div class="fw-semibold">

                                    ${formatPrice(item.price)}
                                    ${cart.currency}

                                </div>

                            </div>


                            <!-- Quantity Control -->

                            <div class="col-6 col-md-3">

                                <div class="text-secondary small mb-1">
                                    Quantity
                                </div>


                                <div class="d-flex align-items-center">

                                    <!-- Decrease -->

                                    <button
                                        type="button"
                                        class="btn btn-outline-light btn-sm decrease-btn"
                                        data-cart-item-id="${item.id}"
                                        data-quantity="${item.quantity}">

                                        <i class="bi bi-dash"></i>

                                    </button>


                                    <!-- Current Quantity -->

                                    <span class="mx-3 fw-semibold">

                                        ${item.quantity}

                                    </span>


                                    <!-- Increase -->

                                    <button
                                        type="button"
                                        class="btn btn-outline-light btn-sm increase-btn"
                                        data-cart-item-id="${item.id}"
                                        data-quantity="${item.quantity}">

                                        <i class="bi bi-plus"></i>

                                    </button>

                                </div>

                            </div>


                            <!-- Subtotal -->

                            <div class="col-6 col-md-2">

                                <div class="text-secondary small">
                                    Subtotal
                                </div>

                                <div class="fw-bold text-success">

                                    ${formatPrice(subtotal)}
                                    ${cart.currency}

                                </div>

                            </div>


                            <!-- Remove -->

                            <div class="col-6 col-md-1 text-md-end">

                                <button
                                    type="button"
                                    class="btn btn-outline-danger btn-sm remove-btn"
                                    data-cart-item-id="${item.id}">

                                    <i class="bi bi-trash"></i>

                                </button>

                            </div>


                        </div>

                    </div>

                </div>
            `;

        }).join("");


    // Đưa Cart Items vào HTML
    cartContainer.innerHTML = itemsHtml;


    // CART SUMMARY BLOCK
    cartSummary.innerHTML = `
        <div class="card bg-dark text-light border-secondary">

            <div class="card-body">

                <div
                    class="d-flex justify-content-between align-items-center">

                    <h5 class="mb-0">
                        Total
                    </h5>

                    <h4 class="mb-0 text-success">

                        ${formatPrice(cart.totalPrice)}
                        ${cart.currency}

                    </h4>

                </div>


                <!-- Checkout -->

                <div class="text-end mt-3">

                    <a
                        class="btn btn-success"
                        href="/checkout">

                        <i class="bi bi-credit-card me-2"></i>

                        Proceed to Checkout

                    </a>

                </div>

            </div>

        </div>
    `;

}


// CART EVENTS
function setupCartEvents() {

    // Lấy Cart Container
    const cartContainerElement =
        document.getElementById("cartContainer");


    // Event Delegation
    cartContainerElement.addEventListener(
        "click",
        function (event) {

            // Tìm button được click
            const button =  event.target.closest("button");


            // Nếu không phải button
            // thì không xử lý
            if (!button) {
                return;
            }


            // INCREASE
            if ( button.classList.contains("increase-btn" )) {
                const selectedItemId =  Number( button.dataset.cartItemId );

                const selectedQuantity = Number( button.dataset.quantity );

                increaseQuantity( selectedItemId, selectedQuantity );

                return;
            }


            // DECREASE
            if (
                button.classList.contains(
                    "decrease-btn"
                )
            ) {

                const selectedItemId =
                    Number(
                        button.dataset.cartItemId
                    );

                const selectedQuantity =
                    Number(
                        button.dataset.quantity
                    );


                decreaseQuantity(
                    selectedItemId,
                    selectedQuantity
                );

                return;
            }


            // REMOVE
            if (
                button.classList.contains(
                    "remove-btn"
                )
            ) {

                const selectedItemId =
                    Number(
                        button.dataset.cartItemId
                    );


                removeCartItem(
                    selectedItemId
                );

                return;
            }

        }
    );

}


// INCREASE QUANTITY

function increaseQuantity(
    cartItemId,
    currentQuantity
) {

    const newQuantity =
        currentQuantity + 1;


    updateQuantity(
        cartItemId,
        newQuantity
    );

}


// DECREASE QUANTITY

function decreaseQuantity(
    cartItemId,
    currentQuantity
) {

    // Không cho quantity nhỏ hơn 1
    if (currentQuantity <= 1) {
        return;
    }


    const newQuantity =
        currentQuantity - 1;


    updateQuantity(
        cartItemId,
        newQuantity
    );

}


// UPDATE QUANTITY

async function updateQuantity(
    cartItemId,
    newQuantity
) {

    try {

        const response =
            await apiFetch(
                `/api/cart/items/${cartItemId}`,
                {
                    method: "PUT",

                    headers: {
                        "Content-Type":
                            "application/json"
                    },

                    body: JSON.stringify({
                        quantity: newQuantity
                    })
                }
            );


        if (!response) {
            return;
        }


        if (!response.ok) {

            throw new Error(
                "Failed to update quantity"
            );

        }


        // Backend trả về Cart mới
        // nên load lại Cart
        await loadCart();


    } catch (error) {

        console.error(error);

    }

}


// REMOVE CART ITEM

async function removeCartItem(cartItemId) {

    try {

        const response =
            await apiFetch(
                `/api/cart/items/${cartItemId}`,
                {
                    method: "DELETE"
                }
            );


        if (!response) {
            return;
        }


        if (!response.ok) {

            throw new Error(
                "Failed to delete item"
            );

        }


        // Load lại Cart sau khi xóa
        await loadCart();


    } catch (error) {

        console.error(error);

    }

}


// FORMAT PRICE

function formatPrice(price) {

    return new Intl.NumberFormat(
        "vi-VN"
    ).format(price);

}