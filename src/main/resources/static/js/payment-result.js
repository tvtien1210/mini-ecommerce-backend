const paymentSuccessEmplement =
    document.getElementById("payment-success");

const paymentFailedEmplement =
    document.getElementById("payment-failed");

const paymentPendingEmplement =
    document.getElementById("payment-pending");


document.addEventListener("DOMContentLoaded", function () {

    paymentSuccessEmplement.style.display = "none";
    paymentFailedEmplement.style.display = "none";
    paymentPendingEmplement.style.display = "none";

    loadPaymentResultPage();

});


function loadPaymentResultPage() {

    // Tim PaymentDTO payment qua txnRef
    // duoc tra ve qua link VNPay return
    // co chua vnp_TxnRef

    const params =
        new URLSearchParams(window.location.search);

    const txnRef =
        params.get("vnp_TxnRef");

    waitForPayment(txnRef);

}


async function waitForPayment(txnRef) {

    for (let attemp = 0; attemp <= 10; attemp++) {


        // Goi API /api/payment/{txnRef}
        // de lay CurrentPaymentDTO
        //
        // payment.order.orderStatus
        // dung de kiem tra trang thai Order

        const response =
            await apiFetch(`/api/payment/${txnRef}`);


        if (!response.ok) {

            console.error(
                "payment api failed",
                response.status
            );

            return;
        }


        const payment =
            await response.json();


        // ==============================
        // PAYMENT SUCCESS
        // ==============================

        if (payment.order.orderStatus == "PAID") {

            paymentSuccessEmplement.style.display = "block";

            paymentSuccessEmplement.innerHTML = `

                <div class="card bg-dark text-white
                            border-secondary shadow">

                    <div class="card-body
                                p-4 p-md-5 text-center">


                        <!-- Success Icon -->

                        <div class="mb-4">

                            <div class="
                                d-inline-flex
                                align-items-center
                                justify-content-center
                                rounded-circle
                                bg-success
                                bg-opacity-10
                                border
                                border-success
                                p-4
                            ">

                                <i class="
                                    bi
                                    bi-check-lg
                                    text-success
                                    fs-1
                                "></i>

                            </div>

                        </div>


                        <!-- Title -->

                        <h2 class="fw-bold mb-2">
                            Payment Successful
                        </h2>


                        <p class="text-secondary mb-4">
                            Thank you for your purchase!
                            Your order has been successfully paid.
                        </p>


                        <!-- Order Information -->

                        <div class="
                            bg-black
                            border
                            border-secondary
                            rounded
                            p-3
                            mb-4
                            text-start
                        ">

                            <div class="
                                d-flex
                                justify-content-between
                                align-items-center
                                mb-3
                            ">

                                <span class="text-secondary">
                                    Order ID
                                </span>

                                <span class="fw-semibold">
                                    #${payment.order.id}
                                </span>

                            </div>


                            <div class="
                                d-flex
                                justify-content-between
                                align-items-center
                            ">

                                <span class="text-secondary">
                                    Status
                                </span>

                                <span class="
                                    badge
                                    text-bg-success
                                ">
                                    PAID
                                </span>

                            </div>

                        </div>


                        <!-- Buttons -->

                        <div class="
                            d-grid
                            gap-2
                            d-md-flex
                            justify-content-md-center
                        ">

                            <a href="/myorders"
                               class="btn btn-success px-4">

                                <i class="
                                    bi
                                    bi-receipt
                                    me-2
                                "></i>

                                View My Orders

                            </a>


                            <a href="/products"
                               class="btn btn-outline-light px-4">

                                <i class="
                                    bi
                                    bi-cart
                                    me-2
                                "></i>

                                Continue Shopping

                            </a>

                        </div>

                    </div>

                </div>

            `;

            return;
        }


        // ==============================
        // PAYMENT FAILED
        // ==============================

        if (payment.order.orderStatus == "CANCELLED") {

            paymentFailedEmplement.style.display = "block";

            paymentFailedEmplement.innerHTML = `

                <div class="card bg-dark text-white
                            border-secondary shadow">

                    <div class="card-body
                                p-4 p-md-5 text-center">


                        <!-- Failed Icon -->

                        <div class="mb-4">

                            <div class="
                                d-inline-flex
                                align-items-center
                                justify-content-center
                                rounded-circle
                                bg-danger
                                bg-opacity-10
                                border
                                border-danger
                                p-4
                            ">

                                <i class="
                                    bi
                                    bi-x-lg
                                    text-danger
                                    fs-1
                                "></i>

                            </div>

                        </div>


                        <!-- Title -->

                        <h2 class="fw-bold mb-2">
                            Payment Failed
                        </h2>


                        <p class="text-secondary mb-4">
                            Unfortunately, your payment
                            could not be completed.
                        </p>


                        <!-- Order Information -->

                        <div class="
                            bg-black
                            border
                            border-secondary
                            rounded
                            p-3
                            mb-4
                            text-start
                        ">

                            <div class="
                                d-flex
                                justify-content-between
                                align-items-center
                                mb-3
                            ">

                                <span class="text-secondary">
                                    Order ID
                                </span>

                                <span class="fw-semibold">
                                    #${payment.order.id}
                                </span>

                            </div>


                            <div class="
                                d-flex
                                justify-content-between
                                align-items-center
                            ">

                                <span class="text-secondary">
                                    Status
                                </span>

                                <span class="
                                    badge
                                    text-bg-danger
                                ">
                                    CANCELLED
                                </span>

                            </div>

                        </div>


                        <!-- Buttons -->

                        <div class="
                            d-grid
                            gap-2
                            d-md-flex
                            justify-content-md-center
                        ">

                            <a href="/orders"
                               class="btn btn-success px-4">

                                <i class="
                                    bi
                                    bi-receipt
                                    me-2
                                "></i>

                                View My Orders

                            </a>


                            <a href="/products"
                               class="btn btn-outline-light px-4">

                                <i class="
                                    bi
                                    bi-cart
                                    me-2
                                "></i>

                                Continue Shopping

                            </a>

                        </div>

                    </div>

                </div>

            `;

            return;
        }


        // ==============================
        // PAYMENT PENDING
        // ==============================

        paymentPendingEmplement.style.display = "block";

        paymentPendingEmplement.innerHTML = `

            <div class="card bg-dark text-white
                        border-secondary shadow">

                <div class="card-body
                            p-4 p-md-5 text-center">


                    <!-- Pending Icon -->

                    <div class="mb-4">

                        <div class="
                            d-inline-flex
                            align-items-center
                            justify-content-center
                            rounded-circle
                            bg-warning
                            bg-opacity-10
                            border
                            border-warning
                            p-4
                        ">

                            <i class="
                                bi
                                bi-hourglass-split
                                text-warning
                                fs-1
                            "></i>

                        </div>

                    </div>


                    <!-- Title -->

                    <h2 class="fw-bold mb-2">
                        Payment Processing
                    </h2>


                    <p class="text-secondary mb-4">
                        We are confirming your payment.
                        Please wait a moment.
                    </p>


                    <!-- Loading -->

                    <div class="
                        d-flex
                        justify-content-center
                        mb-4
                    ">

                        <div class="
                            spinner-border
                            text-success
                        "
                        role="status">

                            <span class="visually-hidden">
                                Loading...
                            </span>

                        </div>

                    </div>


                    <!-- Status -->

                    <div class="
                        bg-black
                        border
                        border-secondary
                        rounded
                        p-3
                        mb-4
                        text-start
                    ">

                        <div class="
                            d-flex
                            justify-content-between
                            align-items-center
                        ">

                            <span class="text-secondary">
                                Status
                            </span>

                            <span class="
                                badge
                                text-bg-warning
                            ">
                                PROCESSING
                            </span>

                        </div>

                    </div>


                    <a href="/orders"
                       class="btn btn-outline-light px-4">

                        <i class="
                            bi
                            bi-receipt
                            me-2
                        "></i>

                        View My Orders

                    </a>

                </div>

            </div>

        `;


        // 1s = 1000ms
        await sleep(1000);

    }


    // ==================================
    // STILL PROCESSING AFTER POLLING
    // ==================================

    paymentPendingEmplement.innerHTML = `

        <div class="card bg-dark text-white
                    border-secondary shadow">

            <div class="card-body
                        p-4 p-md-5 text-center">


                <div class="mb-4">

                    <div class="
                        d-inline-flex
                        align-items-center
                        justify-content-center
                        rounded-circle
                        bg-warning
                        bg-opacity-10
                        border
                        border-warning
                        p-4
                    ">

                        <i class="
                            bi
                            bi-clock-history
                            text-warning
                            fs-1
                        "></i>

                    </div>

                </div>


                <h2 class="fw-bold mb-2">
                    Payment Still Processing
                </h2>


                <p class="text-secondary mb-4">

                    Your payment is still being confirmed.
                    Please check your order again later.

                </p>


                <div class="
                    bg-black
                    border
                    border-secondary
                    rounded
                    p-3
                    mb-4
                    text-start
                ">

                    <div class="
                        d-flex
                        justify-content-between
                        align-items-center
                    ">

                        <span class="text-secondary">
                            Status
                        </span>

                        <span class="
                            badge
                            text-bg-warning
                        ">
                            PENDING
                        </span>

                    </div>

                </div>


                <a href="/orders"
                   class="btn btn-success px-4">

                    <i class="
                        bi
                        bi-receipt
                        me-2
                    "></i>

                    View My Orders

                </a>

            </div>

        </div>

    `;

}


function sleep(ms) {

    return new Promise(
        resolve => setTimeout(resolve, ms)
    );

}

//CHECK BUG RESPONSE CUA PAYMENT
//    async function waitForPayment(txnRef) {
//
//        for (let attempt = 0; attempt <= 10; attempt++) {
//
//            const response =
//                await apiFetch(`/api/payment/${txnRef}`);
//
//            const text = await response.text();
//
//            console.log("STATUS:", response.status);
//            console.log("CONTENT-TYPE:", response.headers.get("content-type"));
//            console.log("RESPONSE:", text);
//
//            // tạm dừng ở đây
//            return;
//
//        }
//    }

