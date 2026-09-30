"""Commerce API tables.

Revision ID: 0002_commerce
Revises: 083712aedfbe
"""

from alembic import op
import sqlalchemy as sa

revision = "0002_commerce"
down_revision = "083712aedfbe"
branch_labels = None
depends_on = None


def upgrade() -> None:
    with op.batch_alter_table("categories") as batch:
        batch.alter_column(
            "name",
            existing_type=sa.String(50),
            type_=sa.String(255),
            existing_nullable=False,
        )
    op.create_table(
        "users",
        sa.Column("id", sa.Integer, primary_key=True),
        sa.Column("username", sa.String(20), nullable=False, unique=True),
        sa.Column("email", sa.String(50), nullable=False, unique=True),
        sa.Column("password_hash", sa.String(120), nullable=False),
    )
    op.create_table(
        "user_roles",
        sa.Column(
            "user_id",
            sa.Integer,
            sa.ForeignKey("users.id", ondelete="CASCADE"),
            primary_key=True,
        ),
        sa.Column("role", sa.String(20), primary_key=True),
    )
    op.create_table(
        "products",
        sa.Column("id", sa.Integer, primary_key=True),
        sa.Column("name", sa.String(255), nullable=False, unique=True),
        sa.Column("description", sa.Text, nullable=False),
        sa.Column("quantity", sa.Integer, nullable=False),
        sa.Column("price", sa.Numeric(12, 2), nullable=False),
        sa.Column("discount", sa.Numeric(12, 2), nullable=False),
        sa.Column(
            "category_id", sa.Integer, sa.ForeignKey("categories.id"), nullable=False
        ),
    )
    op.create_table(
        "addresses",
        sa.Column("id", sa.Integer, primary_key=True),
        sa.Column("user_id", sa.Integer, sa.ForeignKey("users.id"), nullable=False),
        sa.Column("street_line1", sa.String(255), nullable=False),
        sa.Column("street_line2", sa.String(255)),
        sa.Column("city", sa.String(255), nullable=False),
        sa.Column("state", sa.String(255), nullable=False),
        sa.Column("country", sa.String(255), nullable=False),
        sa.Column("zip_code", sa.String(255), nullable=False),
    )
    op.create_table(
        "carts",
        sa.Column("id", sa.Integer, primary_key=True),
        sa.Column(
            "user_id",
            sa.Integer,
            sa.ForeignKey("users.id"),
            nullable=False,
            unique=True,
        ),
        sa.Column("total_price", sa.Numeric(12, 2), nullable=False),
    )
    op.create_table(
        "cart_items",
        sa.Column("id", sa.Integer, primary_key=True),
        sa.Column(
            "cart_id",
            sa.Integer,
            sa.ForeignKey("carts.id", ondelete="CASCADE"),
            nullable=False,
        ),
        sa.Column(
            "product_id", sa.Integer, sa.ForeignKey("products.id"), nullable=False
        ),
        sa.Column("quantity", sa.Integer, nullable=False),
        sa.Column("price", sa.Numeric(12, 2), nullable=False),
        sa.Column("discount", sa.Numeric(12, 2), nullable=False),
        sa.UniqueConstraint("cart_id", "product_id"),
    )
    op.create_table(
        "payments",
        sa.Column("id", sa.Integer, primary_key=True),
        sa.Column("method", sa.String(255), nullable=False),
        sa.Column("pg_name", sa.String(255), nullable=False),
        sa.Column("pg_payment_id", sa.String(255)),
        sa.Column("pg_status", sa.String(255)),
        sa.Column("pg_response", sa.Text),
    )
    op.create_table(
        "orders",
        sa.Column("id", sa.Integer, primary_key=True),
        sa.Column("user_id", sa.Integer, sa.ForeignKey("users.id"), nullable=False),
        sa.Column(
            "address_id", sa.Integer, sa.ForeignKey("addresses.id"), nullable=False
        ),
        sa.Column(
            "payment_id", sa.Integer, sa.ForeignKey("payments.id"), nullable=False
        ),
        sa.Column(
            "order_date", sa.DateTime, nullable=False, server_default=sa.func.now()
        ),
        sa.Column("total_amount", sa.Numeric(12, 2), nullable=False),
        sa.Column("status", sa.String(20), nullable=False),
    )
    op.create_table(
        "order_items",
        sa.Column("id", sa.Integer, primary_key=True),
        sa.Column(
            "order_id",
            sa.Integer,
            sa.ForeignKey("orders.id", ondelete="CASCADE"),
            nullable=False,
        ),
        sa.Column(
            "product_id", sa.Integer, sa.ForeignKey("products.id"), nullable=False
        ),
        sa.Column("quantity", sa.Integer, nullable=False),
        sa.Column("price", sa.Numeric(12, 2), nullable=False),
        sa.Column("discount", sa.Numeric(12, 2), nullable=False),
    )


def downgrade() -> None:
    for table in (
        "order_items",
        "orders",
        "payments",
        "cart_items",
        "carts",
        "addresses",
        "products",
        "user_roles",
        "users",
    ):
        op.drop_table(table)
    with op.batch_alter_table("categories") as batch:
        batch.alter_column(
            "name",
            existing_type=sa.String(255),
            type_=sa.String(50),
            existing_nullable=False,
        )
