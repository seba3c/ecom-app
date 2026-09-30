from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.sql import Select


async def paginate(
    session: AsyncSession,
    query: Select,
    page_number: int,
    page_size: int,
    order_by,
    sort_order: str,
) -> dict:
    count = await session.scalar(select(func.count()).select_from(query.subquery()))
    order = order_by.asc() if sort_order.lower() == "asc" else order_by.desc()
    records = list(
        (
            await session.scalars(
                query.order_by(order).offset(page_number * page_size).limit(page_size)
            )
        ).all()
    )
    total = count or 0
    total_pages = (total + page_size - 1) // page_size
    return {
        "content": records,
        "page_number": page_number,
        "page_size": page_size,
        "total_elements": total,
        "total_pages": total_pages,
        "last_page": page_number >= total_pages - 1,
    }
