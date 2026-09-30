from pydantic import BaseModel as PydanticBaseModel
from pydantic import ConfigDict
from pydantic.alias_generators import to_camel


class BaseModel(PydanticBaseModel):
    """Custom base model for all Pydantic schemas in the app.

    Add global customizations here (e.g., extra='forbid', strict mode,
    custom JSON encoders, or shared validators).
    """

    model_config = {"populate_by_name": True, "from_attributes": True}


class APIModel(BaseModel):
    model_config = ConfigDict(
        from_attributes=True, populate_by_name=True, alias_generator=to_camel
    )
