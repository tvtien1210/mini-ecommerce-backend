const ordersContainerElement =
    document.getElementById("ordersContainer");

const emptyOrdersElement =
    document.getElementById("emptyOrders");


document.addEventListener("DOMContentLoaded", function () {
    loadMyOrdersPage();
});


async function loadMyOrdersPage() {

    try {

        const response = await apiFetch("/api/orders/my");

        if (!response.ok) {
            throw new Error("Failed to load orders");
        }

        const orders = await response.json();

        renderMyOrdersPage(orders);

    } catch (error) {

        console.error("Load my orders failed:", error);

        ordersContainerElement.innerHTML = `
            <div class="alert alert-danger">
                Failed to load your orders.
                Please try again later.
            </div>
        `;
    }
}


function renderMyOrdersPage(orders) {

    try {

        // Không có order
        if (!orders || orders.length === 0) {

            ordersContainerElement.innerHTML = "";

            emptyOrdersElement.classList.remove("d-none");

            return;
        }


        // Có order
        emptyOrdersElement.classList.add("d-none");


        const itemsHTML = orders.map(function (order) {

            let totalProduct = 0;


            for (const orderItem of order.orderItems) {

                totalProduct += orderItem.quantity;

            }


            return `

                <!-- ==================================================
                     ORDER CARD
                ================================================== -->

                <div
                    class="card border-secondary shadow mb-4"
                    style="background-color: #1e1e1e;"
                >

                    <!-- =========================================
                         ORDER HEADER
                    ========================================== -->

                    <div
                        class="card-header border-secondary py-3"
                        style="background-color: #292929;"
                    >

                        <div class="row align-items-center g-3">

                            <!-- Order ID -->

                            <div class="col-12 col-md-3">

                                <small class="text-secondary d-block mb-1">
                                    Order ID
                                </small>

                                <span
                                    class="text-primary fw-bold fs-6"
                                >
                                    ${order.id}
                                </span>

                            </div>


                            <!-- Order Date -->

                            <div class="col-6 col-md-3">

                                <small class="text-secondary d-block mb-1">
                                    Order Date
                                </small>

                                <span class="text-light">
                                    ${order.createdAt}
                                </span>

                            </div>


                            <!-- Status -->

                            <div class="col-6 col-md-3">

                                <small class="text-secondary d-block mb-1">
                                    Status
                                </small>

                                <span
                                    class="badge text-bg-success px-3 py-2"
                                >

                                    <i class="bi bi-check-circle me-1"></i>

                                    ${order.orderStatus}

                                </span>

                            </div>


                            <!-- Total -->

                            <div class="col-12 col-md-3 text-md-end">

                                <small class="text-secondary d-block mb-1">
                                    Total
                                </small>

                                <span
                                    class="text-primary fw-bold fs-5"
                                >
                                    ${order.totalPrice}
                                </span>

                            </div>

                        </div>

                    </div>


                    <!-- =========================================
                         ORDER BODY
                    ========================================== -->

                    <div class="card-body p-4">

                        <div class="row align-items-center">

                            <!-- Order information -->

                            <div class="col-12 col-md-8">

                                <div class="d-flex align-items-center">

                                    <!-- Icon -->

                                    <div
                                        class="rounded border border-secondary p-3 me-3"
                                        style="background-color: #111111;"
                                    >

                                        <i
                                            class="bi bi-box-seam fs-3 text-primary"
                                        ></i>

                                    </div>


                                    <!-- Information -->

                                    <div>

                                        <h6 class="mb-1 text-light">

                                            ${totalProduct} products

                                        </h6>


                                        <p class="text-secondary mb-0">

                                            Your order has been
                                            ${order.orderStatus}

                                        </p>

                                    </div>

                                </div>

                            </div>


                            <!-- View Details -->

                            <div
                                class="col-12 col-md-4 text-md-end mt-3 mt-md-0"
                            >

                                <a
                                    href="#"
                                    class="btn btn-outline-primary"
                                >

                                    View Details

                                    <i
                                        class="bi bi-arrow-right ms-1"
                                    ></i>

                                </a>

                            </div>

                        </div>

                    </div>

                </div>
            `;

        }).join("");


        ordersContainerElement.innerHTML = itemsHTML;

    } catch (error) {

        console.error("Render orders failed:", error);

        ordersContainerElement.innerHTML = `
            <div class="alert alert-danger">
                Failed to display your orders.
            </div>
        `;
    }
}
