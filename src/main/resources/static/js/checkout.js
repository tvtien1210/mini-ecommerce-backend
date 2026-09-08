const checkoutSubtotalElement =
    document.getElementById("checkoutSubtotal");

const checkoutTotalElement =
    document.getElementById("checkoutTotal");

const payButtonElement =
    document.getElementById("payButton");

const checkoutItemsElement =
    document.getElementById("checkoutItems");

const checkoutTaxElement =
    document.getElementById("checkoutTax");


document.addEventListener("DOMContentLoaded", function () {

    loadCheckoutPage();

    setupCheckoutEvents();

});


// ==================================================
// LOAD CHECKOUT PAGE
// ==================================================

async function loadCheckoutPage() {

    try {

        // Get cart
        const response =
            await apiFetch("/api/cart/my");


        if (!response.ok) {

            throw new Error(
                "Failed to load cart"
            );

        }


        const cart =
            await response.json();


        renderCheckoutItems(cart);

        renderCheckoutSubtotal(cart);

        renderCheckoutTax(cart);

        renderCheckoutTotal(cart);


    } catch (error) {

        console.error(
            "Load checkout page failed:",
            error
        );


        checkoutItemsElement.innerHTML = `
            <div class="alert alert-danger">
                Failed to load checkout information.
                Please try again later.
            </div>
        `;

    }

}


// ==================================================
// CALCULATE SUBTOTAL
// ==================================================

function calculateSubtotal(cart) {

    let subTotal = 0;


    for (const item of cart.cartItems) {

        const subTotalItem =
            item.price * item.quantity;

        subTotal += subTotalItem;

    }


    return subTotal;

}


// ==================================================
// ORDER ITEMS
// ==================================================

function renderCheckoutItems(cart) {

    try {

        const itemsHTML =
            cart.cartItems.map(function (item) {

                const itemSubtotal =
                    item.price * item.quantity;


                return `

                    <div class="border-bottom border-secondary py-3">

                        <div class="row align-items-center g-3">


                            <!-- PRODUCT -->

                            <div class="col-12 col-md-5">

                                <div class="d-flex align-items-center">

                                    <div
                                        class="bg-dark border border-secondary
                                               rounded p-2 me-3">

                                        <i class="bi bi-box-seam fs-5"></i>

                                    </div>


                                    <div>

                                        <div class="fw-semibold">

                                            ${item.productName}

                                        </div>


                                        <small class="text-secondary">

                                            Product

                                        </small>

                                    </div>

                                </div>

                            </div>


                            <!-- PRICE -->

                            <div class="col-4 col-md-2">

                                <div class="text-secondary small">

                                    Price

                                </div>


                                <div class="fw-medium">

                                    ${formatPrice(item.price, cart)}

                                </div>

                            </div>


                            <!-- QUANTITY -->

                            <div class="col-4 col-md-2">

                                <div class="text-secondary small">

                                    Quantity

                                </div>


                                <span
                                    class="badge text-bg-secondary">

                                    ${item.quantity}

                                </span>

                            </div>


                            <!-- SUBTOTAL -->

                            <div class="col-4 col-md-3 text-end">

                                <div class="text-secondary small">

                                    Subtotal

                                </div>


                                <div class="fw-semibold text-light">

                                    ${formatPrice(itemSubtotal, cart)}

                                </div>

                            </div>


                        </div>

                    </div>

                `;

            }).join("");


        checkoutItemsElement.innerHTML =
            itemsHTML;


    } catch (error) {

        console.error(
            "Render checkout items failed:",
            error
        );


        throw error;

    }

}


// ==================================================
// SUBTOTAL
// ==================================================

function renderCheckoutSubtotal(cart) {

    try {

        const subTotal =
            calculateSubtotal(cart);


        checkoutSubtotalElement.innerHTML =
            formatPrice(subTotal, cart);


    } catch (error) {

        console.error(
            "Render subtotal failed:",
            error
        );

        throw error;

    }

}


// ==================================================
// TAX
// ==================================================

function renderCheckoutTax(cart) {

    try {

        const subTotal =
            calculateSubtotal(cart);


        const tax =
            subTotal * 0.1;


        checkoutTaxElement.innerHTML =
            formatPrice(tax, cart);


    } catch (error) {

        console.error(
            "Render tax failed:",
            error
        );

        throw error;

    }

}


// ==================================================
// TOTAL
// ==================================================

function renderCheckoutTotal(cart) {

    try {

        const subTotal =
            calculateSubtotal(cart);


        const tax =
            subTotal * 0.1;


        const total =
            subTotal + tax;


        checkoutTotalElement.innerHTML =
            formatPrice(total, cart);


    } catch (error) {

        console.error(
            "Render total failed:",
            error
        );

        throw error;

    }

}


// ==================================================
// CHECKOUT EVENTS
// ==================================================

function setupCheckoutEvents() {

    payButtonElement.addEventListener(
        "click",
        async function () {

            await checkout();

        }
    );

}


// ==================================================
// CHECKOUT
// ==================================================

async function checkout() {

    try {

        const response =
            await apiFetch(
                "/api/orders/checkout",
                {
                    method: "POST"
                }
            );


        if (!response.ok) {

            throw new Error(
                "Checkout request failed"
            );

        }


        const checkoutData =
            await response.json();


        console.log(
            "checkoutData is:",
            checkoutData
        );


        if (!checkoutData.paymentUrl) {

            throw new Error(
                "Payment URL was not returned"
            );

        }


        // Điều hướng đến VNPay
        window.location.href =
            checkoutData.paymentUrl;


    } catch (error) {

        console.error(
            "Checkout failed:",
            error
        );

    }

}


// ==================================================
// FORMAT PRICE
// ==================================================

function formatPrice(price, cart) {

    return new Intl.NumberFormat("vi-VN")
        .format(price)
        + " "
        + cart.currency;

}
