from app.core.exceptions import not_found
from app.models.address import Address
from app.repositories.addresses import AddressRepository
from app.schemas.address import AddressInput


class AddressService:
    def __init__(self, repository: AddressRepository):
        self.repository = repository
        self.session = repository.session

    async def address(self, user_id: int, address_id: int) -> Address:
        address = await self.repository.get(address_id, user_id)
        if address is None:
            raise not_found("Address", address_id)
        return address

    async def create_address(self, user_id: int, data: AddressInput) -> Address:
        address = Address(user_id=user_id, **data.model_dump())
        self.session.add(address)
        await self.session.commit()
        await self.session.refresh(address)
        return address

    async def update_address(
        self, user_id: int, address_id: int, data: AddressInput
    ) -> Address:
        address = await self.address(user_id, address_id)
        for key, value in data.model_dump().items():
            setattr(address, key, value)
        await self.session.commit()
        return address

    async def delete_address(self, user_id: int, address_id: int) -> None:
        address = await self.address(user_id, address_id)
        await self.session.delete(address)
        await self.session.commit()
