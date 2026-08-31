// =====================================================
// API URL
// =====================================================

const API_URL = "/api/products";


// =====================================================
// DOM ELEMENTS
// =====================================================

const productTableBody =
    document.getElementById("productTableBody");

const productMobileList =
    document.getElementById("productMobileList");

const productForm =
    document.getElementById("productForm");

const productModal =
    document.getElementById("productModal");

const deleteModal =
    document.getElementById("deleteModal");


// =====================================================
// VARIABLES
// =====================================================

let currentProducts = [];

let deletingProductId = null;


// =====================================================
// LOAD PRODUCTS
// =====================================================

async function loadProducts() {

    try {

        const response =
            await fetch(API_URL);

        if (!response.ok) {

            throw new Error(
                "Failed to load products"
            );

        }


        const products =
            await response.json();


        currentProducts = products;


        renderDesktopProducts(products);

        renderMobileProducts(products);


    } catch (error) {

        console.error(error);

        showToast(
            "Failed to load products"
        );

    }

}


// =====================================================
// RENDER DESKTOP PRODUCTS
// =====================================================

function renderDesktopProducts(products) {

    if (!productTableBody) {

        return;

    }


    if (products.length === 0) {

        productTableBody.innerHTML = `

            <tr>

                <td
                    colspan="5"
                    class="text-center text-secondary py-5">

                    No products found.

                </td>

            </tr>

        `;

        return;

    }


    productTableBody.innerHTML =
        products.map(product => `

            <tr>


                <!-- PRODUCT -->

                <td class="ps-4">

                    <div
                        class="d-flex
                               align-items-center
                               gap-3">


                        <img
                            src="${product.imageUrl || ""}"
                            alt="${escapeHtml(product.name)}"
                            class="product-image">


                        <div>


                            <div
                                class="fw-semibold
                                       text-white">

                                ${escapeHtml(product.name)}

                            </div>


                            <small
                                class="text-secondary">

                                ID: ${product.id}

                            </small>


                        </div>


                    </div>

                </td>


                <!-- DESCRIPTION -->

                <td>

                    <span class="text-secondary">

                        ${escapeHtml(
                            truncateText(
                                product.description,
                                70
                            )
                        )}

                    </span>

                </td>


                <!-- PRICE -->

                <td>

                    <span
                        class="text-success
                               fw-semibold">

                        ${formatPrice(
                            product.price,
                            product.currency
                        )}

                    </span>

                </td>


                <!-- STOCK -->

                <td>

                    ${getStockBadge(
                        product.stock
                    )}

                </td>


                <!-- ACTIONS -->

                <td
                    class="text-end
                           pe-4">


                    <button
                        type="button"
                        class="btn
                               btn-outline-success
                               btn-sm
                               action-button"
                        onclick="openEditProductModal(${product.id})">

                        <i class="bi bi-pencil"></i>

                    </button>


                    <button
                        type="button"
                        class="btn
                               btn-outline-danger
                               btn-sm
                               action-button
                               ms-1"
                        onclick="openDeleteModal(${product.id})">

                        <i class="bi bi-trash"></i>

                    </button>
                </td>
            </tr>

        `).join("");

}


// =====================================================
// RENDER MOBILE PRODUCTS
// =====================================================

function renderMobileProducts(products) {

    if (!productMobileList) {

        return;

    }


    if (products.length === 0) {

        productMobileList.innerHTML = `

            <div
                class="text-center
                       text-secondary
                       py-5">

                No products found.

            </div>

        `;

        return;

    }


    productMobileList.innerHTML =
        products.map(product => `

            <div
                class="product-mobile-card
                       mb-3">


                <!-- TOP -->

                <div
                    class="d-flex
                           gap-3
                           align-items-start">


                    <!-- IMAGE -->

                    <img
                        src="${product.imageUrl || ""}"
                        alt="${escapeHtml(product.name)}"
                        class="product-mobile-image">


                    <!-- PRODUCT INFO -->

                    <div
                        class="flex-grow-1
                               min-width-0">


                        <!-- NAME -->

                        <h6
                            class="mobile-product-name
                                   fw-semibold
                                   mb-1
                                   text-white">

                            ${escapeHtml(product.name)}

                        </h6>


                        <!-- ID -->

                        <div
                            class="small
                                   text-secondary
                                   mb-2">

                            Product ID:
                            ${product.id}

                        </div>


                        <!-- PRICE -->

                        <div
                            class="text-success
                                   fw-semibold
                                   mb-2">

                            ${formatPrice(
                                product.price,
                                product.currencyCode
                            )}

                        </div>


                        <!-- STOCK -->

                        <div>

                            ${getStockBadge(
                                product.stock
                            )}

                        </div>


                    </div>


                </div>


                <!-- DESCRIPTION -->

                <div
                    class="border-top
                           border-secondary
                           mt-3
                           pt-3">


                    <div
                        class="small
                               text-secondary">

                        ${escapeHtml(
                            product.description
                        )}

                    </div>


                </div>


                <!-- ACTIONS -->

                <div
                    class="d-flex
                           gap-2
                           mt-3">


                    <!-- EDIT -->

                    <button
                        type="button"
                        class="btn
                               btn-outline-success
                               flex-grow-1"
                        onclick="openEditProductModal(${product.id})">


                        <i class="bi bi-pencil me-1"></i>

                        Edit


                    </button>


                    <!-- DELETE -->

                    <button
                        type="button"
                        class="btn
                               btn-outline-danger
                               flex-grow-1"
                        onclick="openDeleteModal(${product.id})">


                        <i class="bi bi-trash me-1"></i>

                        Delete


                    </button>


                </div>


            </div>

        `).join("");

}


// =====================================================
// OPEN ADD PRODUCT MODAL
// =====================================================

window.openAddProductModal =
    function () {

        document.getElementById(
            "productModalTitle"
        ).textContent =
            "Add Product";


        document.getElementById(
            "saveProductButton"
        ).innerHTML = `

            <i class="bi bi-plus-lg me-1"></i>

            Add Product

        `;


        productForm.reset();


        document.getElementById(
            "productId"
        ).value = "";

    };


// =====================================================
// OPEN EDIT PRODUCT MODAL
// =====================================================

window.openEditProductModal =
    function (productId) {

        const product =
            currentProducts.find(
                product =>
                    product.id === productId
            );


        if (!product) {

            showToast(
                "Product not found"
            );

            return;

        }


        document.getElementById(
            "productModalTitle"
        ).textContent =
            "Edit Product";


        document.getElementById(
            "saveProductButton"
        ).innerHTML = `

            <i class="bi bi-check-lg me-1"></i>

            Update Product

        `;


        document.getElementById(
            "productId"
        ).value =
            product.id;


        document.getElementById(
            "productName"
        ).value =
            product.name;


        document.getElementById(
            "productDescription"
        ).value =
            product.description;


        document.getElementById(
            "productPrice"
        ).value =
            product.price;


        document.getElementById(
            "productCurrency"
        ).value =
            product.currency;


        document.getElementById(
            "productStock"
        ).value =
            product.stock;


        document.getElementById(
            "productImageUrl"
        ).value =
            product.imageUrl || "";


        const modal =
            bootstrap.Modal.getOrCreateInstance(
                productModal
            );


        modal.show();

    };


// =====================================================
// SUBMIT PRODUCT FORM
// =====================================================

productForm.addEventListener(
    "submit",
    async function (event) {

        event.preventDefault();


        const productId =
            document.getElementById(
                "productId"
            ).value;


        const productData = {

            name:
                document.getElementById(
                    "productName"
                ).value,

            description:
                document.getElementById(
                    "productDescription"
                ).value,

            price:
                Number(
                    document.getElementById(
                        "productPrice"
                    ).value
                ),

            currency:
                document.getElementById(
                    "productCurrency"
                ).value,

            stock:
                Number(
                    document.getElementById(
                        "productStock"
                    ).value
                ),

            imageUrl:
                document.getElementById(
                    "productImageUrl"
                ).value

        };


        try {

            let response;


            // =============================================
            // CREATE PRODUCT
            // =============================================

            if (!productId) {

                response =
                    await fetch(
                        API_URL,
                        {

                            method:
                                "POST",

                            headers: {

                                "Content-Type":
                                    "application/json"

                            },

                            /*
                             * API cua ban dang nhan:
                             *
                             * List<CreateProductRequest>
                             *
                             * Nen phai gui mang
                             */

                            body:
                                JSON.stringify(
                                    [productData]
                                )

                        }
                    );

            }


            // =============================================
            // UPDATE PRODUCT
            // =============================================

            else {

                response =
                    await fetch(
                        `${API_URL}/${productId}`,
                        {

                            method:
                                "PUT",

                            headers: {

                                "Content-Type":
                                    "application/json"

                            },

                            body:
                                JSON.stringify(
                                    productData
                                )

                        }
                    );

            }


            if (!response.ok) {

                throw new Error(
                    "Failed to save product"
                );

            }


            const modal =
                bootstrap.Modal.getInstance(
                    productModal
                );


            modal.hide();


            showToast(
                productId
                    ? "Product updated successfully"
                    : "Product created successfully"
            );


            await loadProducts();


        } catch (error) {

            console.error(error);


            showToast(
                "Failed to save product"
            );

        }

    }
);


// =====================================================
// OPEN DELETE MODAL
// =====================================================

window.openDeleteModal =
    function (productId) {

        const product =
            currentProducts.find(
                product =>
                    product.id === productId
            );


        if (!product) {

            return;

        }


        deletingProductId =
            productId;


        document.getElementById(
            "deleteProductName"
        ).textContent =
            product.name;


        const modal =
            bootstrap.Modal.getOrCreateInstance(
                deleteModal
            );


        modal.show();

    };


// =====================================================
// CONFIRM DELETE
// =====================================================

window.confirmDelete =
    async function () {

        if (!deletingProductId) {

            return;

        }


        try {

            const response =
                await fetch(
                    `${API_URL}/${deletingProductId}`,
                    {

                        method:
                            "DELETE"

                    }
                );


            if (!response.ok) {

                throw new Error(
                    "Failed to delete product"
                );

            }


            const modal =
                bootstrap.Modal.getInstance(
                    deleteModal
                );


            modal.hide();


            showToast(
                "Product deleted successfully"
            );


            deletingProductId = null;


            await loadProducts();


        } catch (error) {

            console.error(error);


            showToast(
                "Failed to delete product"
            );

        }

    };


// =====================================================
// STOCK BADGE
// =====================================================

function getStockBadge(stock) {

    if (stock <= 0) {

        return `

            <span
                class="badge
                       badge-soft-danger">

                Out of stock

            </span>

        `;

    }


    if (stock <= 5) {

        return `

            <span
                class="badge
                       badge-soft-warning">

                ${stock} left

            </span>

        `;

    }


    return `

        <span
            class="badge
                   badge-soft-success">

            ${stock} in stock

        </span>

    `;

}


// =====================================================
// FORMAT PRICE
// =====================================================

function formatPrice(
    price,
    currency
) {

    return new Intl.NumberFormat(
        "vi-VN",
        {

            style:
                "currency",

            currency:
                currency || "VND",

            maximumFractionDigits:
                0

        }
    ).format(price);

}


// =====================================================
// TRUNCATE TEXT
// =====================================================

function truncateText(
    text,
    maxLength
) {

    if (!text) {

        return "";

    }


    if (text.length <= maxLength) {

        return text;

    }


    return (
        text.substring(
            0,
            maxLength
        )
        + "..."
    );

}


// =====================================================
// ESCAPE HTML
// =====================================================

function escapeHtml(value) {

    if (!value) {

        return "";

    }


    const div =
        document.createElement("div");


    div.textContent =
        value;


    return div.innerHTML;

}


// =====================================================
// TOAST
// =====================================================

function showToast(message) {

    const toastElement =
        document.getElementById(
            "toast"
        );


    const toastMessage =
        document.getElementById(
            "toastMessage"
        );


    toastMessage.textContent =
        message;


    const toast =
        bootstrap.Toast.getOrCreateInstance(
            toastElement
        );


    toast.show();

}


// =====================================================
// INITIAL LOAD
// =====================================================

document.addEventListener(
    "DOMContentLoaded",
    function () {

        loadProducts();

    }
);